package uk.co.eelpieconsulting.feedlistener.daos

import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.model.Category
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class FeedItemDAOTest {

    private val mongoDatabase = "whakaokotest${UUID.randomUUID()}"

    private val mongoHost = run {
        var mongoHost = System.getenv("MONGO_HOST")
        if (mongoHost == null) {
            mongoHost = "localhost"
        }
        mongoHost
    }

    private val dataStoreFactory = DataStoreFactory("mongodb://$mongoHost:27017", mongoDatabase)
    private val subscriptionsDAO = SubscriptionsDAO(dataStoreFactory)
    private val feedItemDAO = FeedItemDAO(dataStoreFactory)

    @Test
    fun canFetchSubscriptionFeedItems() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()
        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription)

        val feedItem = testFeedItemFor(subscription)
        feedItemDAO.add(feedItem)

        val feedItems = feedItemDAO.getSubscriptionFeedItems(subscription, 1)

        assertEquals(1, feedItems.totalCount)
        assertEquals(feedItem.url, feedItems.feedsItems.first().url)
    }

    @Test
    fun canPersistFeedItemCategories() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()
        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription)

        val feedItem = testFeedItemFor(subscription)
        val category = Category("consultations")
        val anotherCategory = Category("news")
        feedItem._categories = listOf(category, anotherCategory)
        feedItemDAO.add(feedItem)

        val feedItems = feedItemDAO.getSubscriptionFeedItems(subscription, 1)

        assertEquals(1, feedItems.totalCount)
        val reloadedCategories = feedItems.feedsItems.first()._categories
        assertEquals(listOf("consultations", "news"), reloadedCategories?.map{it -> it.value})
    }

    @Test
    fun canOverwriteUndatedFeedItems() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()
        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription)

        val url = "http://localhost/" + UUID.randomUUID().toString()
        val feedItem = FeedItem(
            "Without date",
            url,
            null,
            null,
            DateTime.now().toDate(),
            null,
            null,
            null,
            subscription.id,
            subscription.channelId,
            null,
            DateTime.now().toDate()
        )
        feedItemDAO.add(feedItem)

        val betterFeedItem  = FeedItem(
            "With date",
            url,
            null,
            DateTime.now().toDate(),
            DateTime.now().toDate(),
            null,
            null,
            null,
            subscription.id,
            subscription.channelId,
            null,
            DateTime.now().toDate()
        )
        feedItemDAO.add(betterFeedItem)

        val feedItems = feedItemDAO.getSubscriptionFeedItems(subscription, 1)

        assertEquals(1, feedItems.totalCount)
        assertEquals("With date", feedItems.feedsItems.get(0).title)
    }

    @Test
    fun canFetchChannelFeedItems() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()

        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription)
        val anotherSubscription = testSubscription(channel)
        subscriptionsDAO.add(anotherSubscription)

        val feedItem = testFeedItemFor(subscription)
        feedItemDAO.add(feedItem)
        val anotherFeedItem = testFeedItemFor(anotherSubscription)
        feedItemDAO.add(anotherFeedItem)

        val feedItems = feedItemDAO.getChannelFeedItems(channelId = channel.id, 10, 1)

        assertEquals(2, feedItems.totalCount)
    }

    @Test
    fun canFilterChannelFeedItemsBySubscriptions() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()

        val subscription = testSubscription(channel)
        val anotherSubscription = testSubscription(channel)
        val yetAnotherSubscription = testSubscription(channel)

        feedItemDAO.add(testFeedItemFor(subscription))
        feedItemDAO.add(testFeedItemFor(anotherSubscription))
        feedItemDAO.add(testFeedItemFor(yetAnotherSubscription))

        val feedItems = feedItemDAO.getChannelFeedItems(
            channelId = channel.id,
            10,
            1,
            subscriptions = listOf(yetAnotherSubscription.id)
        )

        assertEquals(1, feedItems.totalCount)
    }

    private fun testSubscription(channel: Channel): RssSubscription {
        val subscription = RssSubscription()
        subscription.id = UUID.randomUUID().toString()
        subscription.channelId = channel.id
        return subscription
    }

    private fun testFeedItemFor(subscription: RssSubscription): FeedItem {
        val url = "http://localhost/" + UUID.randomUUID().toString()
        return FeedItem(
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
            null,
            DateTime.now().toDate()
        )
    }
}
