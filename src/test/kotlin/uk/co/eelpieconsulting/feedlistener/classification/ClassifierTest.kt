package uk.co.eelpieconsulting.feedlistener.classification

import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.model.Category
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.rss.classification.Classifier
import uk.co.eelpieconsulting.feedlistener.rss.classification.FeedStatus
import java.util.*

class ClassifierTest {

    private val feedItemDAO = Mockito.mock(FeedItemDAO::class.java)

    private val classifier = Classifier(feedItemDAO)

    @Test
    fun http200WithNoErrorIsOk() {
        val subscription = RssSubscription("http://localhost/ok", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 200
        subscription.error = null
        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(emptyList(), 0L))

        val result = classifier.classify(subscription)

        assertEquals(FeedStatus.ok, result)
    }

    @Test
    fun http404IsGone() {
        val subscription = RssSubscription("http://localhost/gone", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 404
        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(emptyList(), 0L))

        val result = classifier.classify(subscription)

        assertEquals(FeedStatus.gone, result)
    }

    @Test
    fun badHttpCodeButRecentContentIsWobbly() {
        // ie. the feed reading service has lost it's own connection. We don't want a back off when reading resumes.
        val subscription = RssSubscription("http://localhost/gone", UUID.randomUUID().toString(), "a-user")
        subscription.latestItemDate = DateTime.now().minusDays(1).toDate()
        subscription.httpStatus = -1
        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(emptyList(), 0L))

        val result = classifier.classify(subscription)

        assertEquals(FeedStatus.wobbling, result)
    }

    @Test
    fun subscriptionWithRegularPostsShouldBeClassifiedAsFrequent() {
        // ie. the feed reading service has lost it's own connection. We don't want a back off when reading resumes.
        val subscription = RssSubscription("http://localhost/recent", UUID.randomUUID().toString(), "a-user")
        subscription.latestItemDate = DateTime.now().minusDays(1).toDate()
        subscription.httpStatus = 200

        val feedsItems = listOf(
            testFeedItemFor(subscription).copy(date = DateTime.now().minusDays(1).toDate()),
            testFeedItemFor(subscription).copy(date = DateTime.now().minusDays(7).toDate()),
            testFeedItemFor(subscription).copy(date = DateTime.now().minusDays(10).toDate())
        )

        `when`(feedItemDAO.getSubscriptionFeedItems(subscription, 1, 20)).thenReturn(FeedItemsResult(feedsItems, feedsItems.size.toLong()))

        val frequency = classifier.frequency(subscription)

        assertEquals("108.0 / 50.91168824543142", frequency)
    }

    private fun testFeedItemFor(subscription: RssSubscription, categories: List<Category>? = null): FeedItem {
        val url = "http://localhost/" + UUID.randomUUID().toString()
        return FeedItem(
            ObjectId.get(),
            UUID.randomUUID().toString(),
            url,
            null,
            DateTime.now().toDate(),
            DateTime.now().toDate(),
            null,
            null,
            null,
            subscription.id,
            subscription.channelId,
            categories,
            DateTime.now().toDate()
        )
    }
}