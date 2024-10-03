package uk.co.eelpieconsulting.feedlistener.classification

import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import uk.co.eelpieconsulting.feedlistener.TestData
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.rss.classification.Classifier
import uk.co.eelpieconsulting.feedlistener.rss.classification.FeedStatus
import java.util.*

class ClassifierTest : TestData {

    private val feedItemDAO = Mockito.mock(FeedItemDAO::class.java)

    private val classifier = Classifier(feedItemDAO)

    @Test
    fun http200WithNoErrorIsOk() {
        val subscription = RssSubscription("http://localhost/ok", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 200
        subscription.error = null
        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(emptyList(), 0L))

        val result = classifier.classify(subscription)

        assertEquals(setOf(FeedStatus.ok), result)
    }

    @Test
    fun http404IsGone() {
        val subscription = RssSubscription("http://localhost/gone", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 404
        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(emptyList(), 0L))

        val result = classifier.classify(subscription)

        assertEquals(setOf(FeedStatus.gone), result)
    }

    @Test
    fun badHttpCodeButRecentContentIsWobbly() {
        // ie. the feed reading service has lost it's own connection. We don't want a back off when reading resumes.
        val subscription = RssSubscription("http://localhost/gone", UUID.randomUUID().toString(), "a-user")
        subscription.latestItemDate = DateTime.now().minusDays(1).toDate()
        subscription.httpStatus = -1
        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(emptyList(), 0L))

        val result = classifier.classify(subscription)

        assertEquals(setOf(FeedStatus.wobbling), result)
    }

    @Test
    fun subscriptionWithRegularPostsShouldBeClassifiedAsFrequent() {
        // ie. the feed reading service has lost it's own connection. We don't want a back off when reading resumes.
        val subscription = RssSubscription("http://localhost/recent", UUID.randomUUID().toString(), "a-user")
        subscription.latestItemDate = DateTime.now().minusDays(1).toDate()
        subscription.httpStatus = 200

        val feedsItems = listOf(
            testFeedItemFor(subscription, date = DateTime.now().minusDays(1)),
            testFeedItemFor(subscription, date = DateTime.now().minusDays(7)),
            testFeedItemFor(subscription, date = DateTime.now().minusDays(10))
        )

        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(feedsItems, feedsItems.size.toLong()))

        val frequency = classifier.frequency(subscription)

        val fourAndAHalfDays = Days.days(4).toStandardDuration().plus(Hours.hours(12).toStandardDuration())
        assertEquals(fourAndAHalfDays, frequency!!)
    }

}