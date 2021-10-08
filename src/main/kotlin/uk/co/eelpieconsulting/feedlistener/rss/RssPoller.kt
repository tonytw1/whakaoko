package uk.co.eelpieconsulting.feedlistener.rss

import com.github.kittinunf.result.Result
import com.google.common.base.Strings
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import org.apache.logging.log4j.LogManager
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
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import uk.co.eelpieconsulting.feedlistener.rss.classification.Classifier
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.function.Consumer

@Component
class RssPoller @Autowired constructor(val subscriptionsDAO: SubscriptionsDAO, val taskExecutor: TaskExecutor,
                                       val feedFetcher: FeedFetcher, val feedItemDestination: FeedItemDAO,
                                       val feedItemLatestDateFinder: FeedItemLatestDateFinder,
                                       val httpFetcher: HttpFetcher,
                                       val classifier: Classifier,
                                       meterRegistry: MeterRegistry) {

    private val log = LogManager.getLogger(RssPoller::class.java)

    val rssAddedItems = meterRegistry.counter("rss_added_items")
    val rssSuccessesEtagged = meterRegistry.counter("rss_successes", "etagged", "false")
    val rssSuccessesNotEtagged = meterRegistry.counter("rss_successes", "etagged", "true")

    val rssErrors = Counter.builder("rss_errors")
            .description("Errors while reading rss feeds")
            .tags("http_status", "", "exception_name", "")
            .register(meterRegistry)

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

            fun pollFeed(): Result<Subscription, FeedFetchingException> {
                log.info("Fetching full feed: " + subscription.url)
                feedFetcher.fetchFeed(subscription).fold(
                        { successfulFetch ->
                            val maybeFetchedFeed = successfulFetch.first
                            val httpResult = successfulFetch.second

                            // Capture useful headers from all successful fetches
                            subscription.error = null
                            subscription.httpStatus = httpResult.status

                            // Etag
                            val etagHeader = httpResult.headers["Etag"].firstOrNull()
                            if (!Strings.isNullOrEmpty(etagHeader)) {
                                rssSuccessesEtagged.increment()
                            } else {
                                rssSuccessesNotEtagged.increment()
                            }
                            subscription.etag = etagHeader

                            // Last-Modified
                            val lastModifiedHeader = httpResult.headers["Last-Modified"].firstOrNull()
                            val lastModified = lastModifiedHeader?.let {
                                log.info("Saw last-modified header ${lastModifiedHeader} on url ${subscription.url}")
                                try {
                                    val parsed = ZonedDateTime.parse(lastModifiedHeader, DateTimeFormatter.RFC_1123_DATE_TIME)
                                    Date.from(parsed.toInstant())
                                } catch (dtpe: DateTimeParseException) {
                                    log.warn("Could not parse last modified header '${lastModifiedHeader}'")
                                    null
                                }
                            }
                            subscription.lastModified = lastModified

                            // If this fetch returned a full feed response then process the feed items
                            maybeFetchedFeed?.let { fetchedFeed ->
                                // Your fetch returned a feed. This indicates the feed has been updated or
                                // the feed server didn't support our last modified or etag headers
                                log.info("Fetched feed: " + fetchedFeed.feedName)
                                persistFeedItems(fetchedFeed.feedItems)

                                // TODO needs to be a common concern with all subscriptions types; ie Twitter etc
                                val itemCount = feedItemDAO.getSubscriptionFeedItemsCount(subscription.id)
                                val latestItemDate = feedItemLatestDateFinder.getLatestItemDate(fetchedFeed.feedItems)

                                // Backfill the subscription name with the feed title if not already set
                                if (Strings.isNullOrEmpty(subscription.name)) {
                                    subscription.name = fetchedFeed.feedName
                                }
                                subscription.itemCount = itemCount
                                subscription.latestItemDate = latestItemDate

                                log.info("Completed fetch of feed named '${fetchedFeed.feedName}' with ${fetchedFeed.feedItems.size} items from '${subscription.url}'. Latest feed item date was ${latestItemDate}")
                                return Result.success(subscription)
                            }

                            // Our fetch return successfully but with not feed contents
                            // This indicates that the feed server responsed with a not modified response
                            // subscription.httpStatus = fetchedFeed.httpStatus TODO need the not modified response code
                            log.info("Feed fetch returned unmodified for subscription: ${subscription.name}")
                            return Result.success(subscription)

                        },
                        { ex ->
                            log.warn("Fetch feed returning error: " + ex)
                            return Result.error(ex)
                        }
                )
            }

            pollFeed().fold(
                    { updatedSubscription ->
                        log.info("Feed polled with no errors: " + subscription.url)

                    }, { feedFetchingException ->

                val rootCauseName = feedFetchingException.rootCause?.javaClass?.simpleName.orEmpty()
                val httpStatus = feedFetchingException.httpStatus

                log.warn("Exception while fetching RSS subscription: " + subscription.url + ": " + rootCauseName)

                val errorMessage = feedFetchingException.message
                log.info("Setting feed error to: " + errorMessage + "; http status: " + httpStatus)
                subscription.error = errorMessage
                subscription.httpStatus = httpStatus

                Metrics.counter("rss_errors", "http_status", httpStatus.toString(), "exception_name", rootCauseName).increment()
            }
            )

            subscription.classification = classifier.classify(subscription)
            subscriptionsDAO.save(subscription)
        }

        private fun persistFeedItems(feedItems: List<FeedItem>) {
            feedItems.forEach { feedItem ->
                try {
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
        return feedItems.map { it.date }.stream().max(Date::compareTo).orElse(null)
    }

}