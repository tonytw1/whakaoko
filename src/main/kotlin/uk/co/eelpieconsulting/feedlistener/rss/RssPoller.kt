package uk.co.eelpieconsulting.feedlistener.rss

import com.github.kittinunf.result.Result
import com.google.common.base.Strings
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTime
import org.joda.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.views.json.JsonSerializer
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.kafka.Kafka
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import uk.co.eelpieconsulting.feedlistener.rss.classification.Classifier
import uk.co.eelpieconsulting.feedlistener.rss.classification.FeedStatus
import java.net.MalformedURLException
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@Component
class RssPoller @Autowired constructor(
    val subscriptionsDAO: SubscriptionsDAO,
    private val feedFetcher: FeedFetcher, val feedItemDAO: FeedItemDAO,
    val feedItemLatestDateFinder: FeedItemLatestDateFinder,
    val classifier: Classifier,
    val kafka: Kafka,
    @Value("\${kafka.channels}") val kafkaChannels: Array<String>,
    meterRegistry: MeterRegistry
) {

    private val log = LogManager.getLogger(RssPoller::class.java)

    val rssAddedItems: Counter = meterRegistry.counter("rss_added_items")
    val rssSuccessesEtagged: Counter = meterRegistry.counter("rss_successes", "etagged", "false")
    val rssSuccessesNotEtagged: Counter = meterRegistry.counter("rss_successes", "etagged", "true")

    val fixedThreadPoolContext = newFixedThreadPoolContext(1, "rssPoller")
    val limitedDispatcher = fixedThreadPoolContext.limitedParallelism(100)

    @Scheduled(fixedRate = 3600000, initialDelay = 300000)
    fun run() {
        log.info("Polling subscriptions")
        val start = DateTime.now()
        val all = subscriptionsDAO.allRssSubscriptions()
        val toFetch = all.filter { shouldFetchNow(it) }
        runBlocking {
            toFetch.map { subscription ->
                launch(limitedDispatcher) {
                    executeRssPoll(subscription)
                }
            }
        }

        val duration = Duration(start, DateTime.now())
        log.info("Done polling ${toFetch.size} of ${all.size} subscriptions in ${duration.millis}ms")
    }

    fun requestRead(subscription: RssSubscription) {
        log.info("Requesting background read of RSS subscription: " + subscription.name + " / " + subscription.url)
        CoroutineScope(limitedDispatcher).launch {
            executeRssPoll(subscription)
        }
        log.info("Read requested; returning")
    }

    private fun shouldFetchNow(subscription: RssSubscription): Boolean {
        val lastRead = subscription.lastRead
            ?: // If never read then read now
            return true

        val readInterval = fetchIntervalFor(subscription)

        return lastRead.before(DateTime.now().minus(readInterval).toDate())
    }

    fun fetchIntervalFor(subscription: Subscription): Duration? {
        val oneHour = Duration.standardHours(1)
        val oneDay = Duration.standardDays(1)
        val okHttpStatuses = setOf(FeedStatus.ok, FeedStatus.wobbling)

        val classifications = subscription.classifications ?: emptySet()
        val readInterval = if (classifications.intersect(okHttpStatuses).isNotEmpty()) {
            if (classifications.contains(FeedStatus.frequent)) {
                oneHour
            } else {
                // infrequent feeds are only read once a day.
                oneDay
            }
        } else {
            // broken and gone feeds are only read once a day to look for a potential resurrection.
            oneDay
        }
        return readInterval
    }

    suspend private fun executeRssPoll(subscription: RssSubscription) {
        log.info("Processing feed: " + subscription + " from thread " + Thread.currentThread().id)
        val start = DateTime.now()

            if (!hasValidUrl(subscription)) {
                log.warn("Invalid URL: ${subscription.url}")
                subscription.classifications = setOf(FeedStatus.invalid)
                subscription.lastRead = DateTime.now().toDate()
                subscriptionsDAO.save(subscription)
                return
            }

        suspend fun pollFeed(): Result<RssSubscription, FeedFetchingException> {
            log.info("Fetching full feed: " + subscription.url)

            try {
                return feedFetcher.fetchFeed(subscription).fold(
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
                            log.info("Saw last-modified header $lastModifiedHeader on url ${subscription.url}")
                            try {
                                val parsed =
                                    ZonedDateTime.parse(lastModifiedHeader, DateTimeFormatter.RFC_1123_DATE_TIME)
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
                            log.info("Fetched feed: " + fetchedFeed.feedName + " with " + fetchedFeed.feedItems.size + " feed items")
                            persistFeedItems(fetchedFeed.feedItems)

                            val itemCount = feedItemDAO.getSubscriptionFeedItemsCount(subscription.id)
                            val latestItemDate = feedItemLatestDateFinder.getLatestItemDate(fetchedFeed.feedItems)

                            // Backfill the subscription name with the feed title if not already set
                            if (Strings.isNullOrEmpty(subscription.name)) {
                                subscription.name = fetchedFeed.feedName
                            }
                            subscription.itemCount = itemCount
                            subscription.latestItemDate = latestItemDate

                            log.info("Completed fetch of feed named '${fetchedFeed.feedName}' with ${fetchedFeed.feedItems.size} items from '${subscription.url}'. Latest feed item date was $latestItemDate")
                            return Result.success(subscription)
                        }

                        // Our fetch return successfully but with no feed contents
                        // This normal indicates that the feed server responded with a not modified response
                        log.info("Feed fetch returned no feed items / HTTP ${subscription.httpStatus} for subscription: ${subscription.name}")
                        Result.success(subscription)

                    },
                    { ex ->
                        log.warn("Fetch feed returning error", ex)
                        Result.error(ex)
                    }
                )

            } catch (e: Exception) {
                return Result.error(FeedFetchingException(e.message.toString(), null, e))
            }
        }

        pollFeed().fold(
            { updatedSubscription ->
                val duration = Duration(start, DateTime.now())
                log.info("Feed polled in ${duration.millis}ms with no errors: " + updatedSubscription.url)

            }, { feedFetchingException ->
                val duration = Duration(start, DateTime.now())
                val rootCauseName = feedFetchingException.rootCause?.javaClass?.simpleName.orEmpty()
                log.warn("Exception after ${duration.millis}ms while fetching RSS subscription '${subscription.id}': ${subscription.url}: " + rootCauseName)

                val errorMessage = feedFetchingException.message
                val httpStatus = feedFetchingException.httpStatus
                log.info("Setting feed error to: $errorMessage; http status: $httpStatus")
                subscription.error = errorMessage
                subscription.httpStatus = httpStatus

                Metrics.counter("rss_errors", "http_status", httpStatus.toString(), "exception_name", rootCauseName)
                    .increment()
            }
        )

        subscription.classifications = classifier.classify(subscription)
        subscription.lastRead = DateTime.now().toDate()
        subscriptionsDAO.save(subscription)
    }

    private fun persistFeedItems(feedItems: List<FeedItem>) {
        feedItems.forEach { feedItem ->
            val existingSubscriptionFeeditemsWithSameUrl = feedItemDAO.getExistingFeedItemByUrlAndSubscription(feedItem)
            val shouldAdd = existingSubscriptionFeeditemsWithSameUrl.first() == null
            if (shouldAdd) {
                val withAccepted = feedItem.copy(accepted = DateTime.now().toDate())

                // Echo new feed items on to kafka
                val channelHasKafkaTopic = kafkaChannels.contains(feedItem.channelId)
                if (channelHasKafkaTopic) {
                    val asJson = JsonSerializer().serialize(withAccepted)
                    kafka.publish(feedItem.channelId, asJson)
                }

                if (feedItemDAO.add(withAccepted)) {
                    rssAddedItems.increment()

                } else {
                    log.warn("Failed to add feed item: " + feedItem.title)
                }

            } else {
                log.debug("Skipping previously added: " + feedItem.title)
            }
        }
    }

    private fun hasValidUrl(subscription: RssSubscription): Boolean {
        return try {
            val parsed = URL(subscription.url)
            !parsed.host.contains(" ")
        } catch (m: MalformedURLException) {
            false
        }
    }
}

@Component
class FeedItemLatestDateFinder {
    fun getLatestItemDate(feedItems: List<FeedItem>): Date? {
        // Map to dates; return max
        return feedItems.mapNotNull { it.date }.stream().max(Date::compareTo).orElse(null)
    }
}