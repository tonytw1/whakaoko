package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import com.sun.syndication.io.FeedException
import io.micrometer.core.instrument.MeterRegistry
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.http.HttpFetchException
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*
import java.util.function.Consumer

@Component
class RssPoller @Autowired constructor(val subscriptionsDAO: SubscriptionsDAO, val taskExecutor: TaskExecutor,
                                       val feedFetcher: FeedFetcher, val feedItemDestination: FeedItemDAO,
                                       meterRegistry: MeterRegistry) {

    private val log = Logger.getLogger(RssPoller::class.java)

    val rssAddedItems = meterRegistry.counter("rss_added_items")
    val rssSuccessesEtagged = meterRegistry.counter("rss_successes", "etagged", "false")
    val rssSuccessesNotEtagged = meterRegistry.counter("rss_successes", "etagged", "true")

    @Scheduled(fixedRate = 3600000, initialDelay = 300000)
    fun run() {
        log.info("Polling subscriptions")
        subscriptionsDAO.allRssSubscriptions.forEach(Consumer { subscription -> executeRssPoll(subscription) })
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
            try {
                val fetchedFeed = feedFetcher.fetchFeed(subscription.url)
                log.info("Fetched feed: " + fetchedFeed.feedName)
                log.info("Etag: " + fetchedFeed.etag)
                if (!Strings.isNullOrEmpty(fetchedFeed.etag)) {
                    rssSuccessesEtagged.increment()
                } else {
                    rssSuccessesNotEtagged.increment()
                }
                persistFeedItems(fetchedFeed)
                subscription.name = fetchedFeed.feedName
                subscription.error = null
                subscription.etag = fetchedFeed.etag
                subscription.latestItemDate = getLatestItemDate(fetchedFeed.feedItems)
                subscriptionsDAO.save(subscription)
                log.info("Completed feed fetch for: " + fetchedFeed.feedName + "; saw " + fetchedFeed.feedItems.size + " items")

            } catch (e: HttpFetchException) {
                log.warn("Http fetch exception while fetching RSS subscription: " + subscription.url + ": " + e.javaClass.simpleName)
                subscription.error = "Http fetch: " + e.message
                subscriptionsDAO.save(subscription)
            } catch (e: FeedException) {
                log.warn("Feed exception while parsing RSS subscription: " + subscription.url + ": " + e.message)
                subscription.error = "Feed exception: " + e.message
                subscriptionsDAO.save(subscription)
            } catch (e: Exception) {
                log.error("Unexpected error while processing feed: " + subscription.url + ": " + e.message)
                subscription.error = "Feed exception: " + e.message
                subscriptionsDAO.save(subscription)
            }
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

        private fun getLatestItemDate(feedItems: List<FeedItem>): Date? {
            var latestItemDate: Date? = null
            for (feedItem in feedItems) {   // TODO port to a stream operation
                val feedItemDate = feedItem.date
                if (feedItemDate != null && (latestItemDate == null || feedItemDate.after(latestItemDate))) {
                    latestItemDate = feedItemDate
                }
            }
            return latestItemDate
        }
    }

}