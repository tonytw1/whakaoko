package uk.co.eelpieconsulting.feedlistener.daos

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.TestData
import uk.co.eelpieconsulting.feedlistener.model.Category
import uk.co.eelpieconsulting.feedlistener.model.Channel
import java.util.*

class FeedItemDAOTest: TestData {

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
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
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
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription)

        val category = Category("consultations")
        val anotherCategory = Category("news")
        val feedItem = testFeedItemFor(subscription, listOf(category, anotherCategory))

        feedItemDAO.add(feedItem)

        val feedItems = feedItemDAO.getSubscriptionFeedItems(subscription, 1)

        assertEquals(1, feedItems.totalCount)
        val first = feedItems.feedsItems.first()
        val reloadedCategories = first._categories
        assertEquals(listOf("consultations", "news"), reloadedCategories?.map{it.value})

        assertNotNull(first.accepted)
        assertEquals(feedItem.accepted, first.accepted)
        assertNotNull(first.ordering)
        assertEquals(feedItem.ordering, first.ordering)
    }

    @Test
    fun canFetchChannelFeedItems() {
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
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
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
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
        assertEquals(yetAnotherSubscription.id, feedItems.feedsItems[0].subscriptionId)
    }

}
