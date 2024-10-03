package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import kotlin.math.roundToLong

@Component
class Classifier  @Autowired constructor(private val feedItemDAO: FeedItemDAO)  {

    private val log = LogManager.getLogger(Classifier::class.java)

    private val goodHttpCodes = setOf(200, 304)
    private val badHttpCodes = setOf(404, 401, -1)
    private val sevenDays = Days.SEVEN.toStandardDuration()

    fun classify(subscription: Subscription): Set<FeedStatus> {
        val frequencyStatus = frequency(subscription)?.let {
            if (it < sevenDays) {
                FeedStatus.frequent
            } else {
                null
            }
        }

        val livenessStatus = when (subscription) {
            is RssSubscription -> livenessStatus(subscription)
            else -> null
        }
        
        return setOf(frequencyStatus, livenessStatus).mapNotNull { it }.toSet()
    }

    fun frequency(subscription: Subscription): Duration? {
        // Given a subscription estimate the frequency of posts by looking at the gaps between posts

        val subscriptionFeedItems = feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)
        val feedItems = subscriptionFeedItems.feedsItems

        val itemDates = feedItems.mapNotNull {
            if (it.date != null) it.date else it.accepted
        }
        if (itemDates.size < 3) {
            return null
        }

        val stats = DescriptiveStatistics()
        for (i: Int in 0 until feedItems.size - 1) {
            val duration = Duration(DateTime(itemDates[i + 1]), DateTime(itemDates[i]))
            stats.addValue(duration.millis.toDouble())
        }

        val meanDurationBetweenPosts = Duration(stats.mean.roundToLong())
        val standardDeviation = Duration(stats.standardDeviation.roundToLong())
        log.info("Item frequency stats for ${subscription.name}: ${meanDurationBetweenPosts} with ${standardDeviation} standard deviation")
        return meanDurationBetweenPosts
    }

    private fun livenessStatus(subscription: RssSubscription) =
        if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error == null) {
            FeedStatus.ok
        } else if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error != null) {
            FeedStatus.broken
        } else if (badHttpCodes.contains(subscription.httpStatus)) {
            if (subscription.latestItemDate == null) {
                FeedStatus.gone
            } else {
                if (DateTime(subscription.latestItemDate).plusDays(3).isAfterNow) FeedStatus.wobbling else FeedStatus.gone
            }
        } else {
            null
        }

}
