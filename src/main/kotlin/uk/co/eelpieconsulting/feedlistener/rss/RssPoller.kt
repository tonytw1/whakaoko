package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import io.micrometer.core.instrument.MeterRegistry
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException
import uk.co.eelpieconsulting.feedlistener.http.HttpFetcher
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*
import java.util.function.Consumer
import com.github.kittinunf.result.Result

@Component
class RssPoller @Autowired constructor(val subscriptionsDAO: SubscriptionsDAO, val taskExecutor: TaskExecutor,
                                       val feedFetcher: FeedFetcher, val feedItemDestination: FeedItemDAO,
                                       val feedItemLatestDateFinder: FeedItemLatestDateFinder,
                                       val httpFetcher: HttpFetcher,
                                       meterRegistry: MeterRegistry) {

    private val log = Logger.getLogger(RssPoller::class.java)

    val rssAddedItems = meterRegistry.counter("rss_added_items")
    val rssSuccessesEtagged = meterRegistry.counter("rss_successes", "etagged", "false")
    val rssSuccessesNotEtagged = meterRegistry.counter("rss_successes", "etagged", "true")

    @Scheduled(fixedRate = 3600000, initialDelay = 300000)
    fun run() {
        log.info("Polling subscriptions")
        subscriptionsDAO.allRssSubscriptions().forEach(Consumer { subscription -> executeRssPoll(subscription) })
        log.info("Done")
    }

    fun run(subscription: RssSubscription) {
        log.info("Polling single subscription: " + subscription.id)
        executeRssPoll(subscription)
        log.info("Done")
    }

    private fun executeRssPoll(subscription: RssSubscription) {
        log.info("Executing RSS poll for: " + subscription.id)
        val threadPoolTaskExecutor = taskExecutor as ThreadPoolTaskExecutor
        log.info("Task executor: active:" + threadPoolTaskExecutor.activeCount + ", pool size: " + threadPoolTaskExecutor.poolSize)
        taskExecutor.execute(ProcessFeedTask(feedFetcher, feedItemDestination, subscriptionsDAO, subscription))
    }

    private inner class ProcessFeedTask(private val feedFetcher: FeedFetcher, private val feedItemDAO: FeedItemDAO, private val subscriptionsDAO: SubscriptionsDAO, private val subscription: RssSubscription) : Runnable {

        override fun run() {
            log.info("Processing feed: " + subscription + " from thread " + Thread.currentThread().id)
            subscription.lastRead = DateTime.now().toDate()
            subscriptionsDAO.save(subscription)

            fun fetchFullFeed(url: String): Result<Unit, Exception> {
                feedFetcher.fetchFeed(subscription.url).fold(
                        { fetchedFeed ->
                            log.info("Fetched feed: " + fetchedFeed.feedName)
                            log.info("Etag: " + fetchedFeed.etag)
                            if (!Strings.isNullOrEmpty(fetchedFeed.etag)) {
                                rssSuccessesEtagged.increment()
                            } else {
                                rssSuccessesNotEtagged.increment()
                            }
                            persistFeedItems(fetchedFeed)
                            subscription.name = fetchedFeed.feedName
                            subscription.latestItemDate = feedItemLatestDateFinder.getLatestItemDate(fetchedFeed.feedItems)
                            log.info("Completed feed fetch for: " + fetchedFeed.feedName + "; saw " + fetchedFeed.feedItems.size + " items")

                            subscriptionsDAO.save(subscription)
                            subscription.etag = fetchedFeed.etag
                            return Result.success(Unit)
                        },
                        { ex ->
                            return Result.error(ex)
                        }
                )
            }

            // If this feed has an etag we may be able to skip a full read this time
            if (subscription.etag != null) {
                log.info("Checking feed etag before fetching")
                httpFetcher.head(subscription.url).fold({ headers ->
                    val currentEtag = headers["Etag"].stream().findFirst().orElse(null)
                    if (currentEtag != null && currentEtag == subscription.etag) {
                        log.info("Feed etag has no changed; skipping fetch")
                        return
                    }

                }, { ex ->
                    // TODO duplicated with below
                    log.warn("Http fetch exception while fetching RSS subscription: " + subscription.url + ": " + ex.javaClass.simpleName)
                    val errorMessage = ex.message
                    log.info("Setting feed error to: " + errorMessage)
                    subscription.error = errorMessage
                    subscriptionsDAO.save(subscription)
                    return
                })
            }

            fetchFullFeed(subscription.url).fold(
                    { _ ->
                        subscription.error = null
                        subscriptionsDAO.save(subscription)

                    }, { ex ->
                        log.warn("Exception while fetching RSS subscription: " + subscription.url + ": " + ex.javaClass.simpleName)
                        val errorMessage = ex.message
                        log.info("Setting feed error to: " + errorMessage)
                        subscription.error = errorMessage
                        subscriptionsDAO.save(subscription)
                    }
            )
        }

        private fun persistFeedItems(fetchedFeed: FetchedFeed) {
            for (feedItem in fetchedFeed.feedItems) {
                try {
                    feedItem.subscriptionId = subscription.id
                    if (feedItemDAO.add(feedItem)) {
                        rssAddedItems.increment()
                    }
                } catch (e: FeeditemPersistanceException) {
                    log.error(e)
                }
            }
        }
    }
}

@Component
class FeedItemLatestDateFinder {

    fun getLatestItemDate(feedItems: List<FeedItem>): Date? {
        // Map to dates; filter out nulls; return max
        return feedItems.map { it.date }.filterNotNull().stream().max(Date::compareTo).orElse(null)
    }

}