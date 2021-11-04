package uk.co.eelpieconsulting.feedlistener.daos

import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class FeedItemDAOTest {

    private val mongoDatabase = "whakaokotest" + UUID.randomUUID().toString();

    private val mongoHost = run {
        var mongoHost = System.getenv("MONGO_HOST");
        if (mongoHost == null) {
            mongoHost = "localhost";
        }
        mongoHost
    }

    val dataStoreFactory = DataStoreFactory("mongodb://" + mongoHost + ":27017", mongoDatabase)
    val subscriptionsDAO = SubscriptionsDAO(dataStoreFactory);
    val feedItemDAO = FeedItemDAO(dataStoreFactory, subscriptionsDAO)

    @Test
    fun canFetchSubscriptionFeedItems() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()
        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription);

        val feedItem = testFeedItemFor(subscription)
        feedItemDAO.add(feedItem);

        val feedItems = feedItemDAO.getSubscriptionFeedItems(subscription, 1)

        assertEquals(1, feedItems.totalCount)
        assertEquals(feedItem.url, feedItems.feedsItems.first().url)
    }

    @Test
    fun canFetchChannelFeedItems() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()

        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription);
        val anotherSubscription = testSubscription(channel)
        subscriptionsDAO.add(anotherSubscription);

        val feedItem = testFeedItemFor(subscription)
        feedItemDAO.add(feedItem);
        val anotherFeedItem = testFeedItemFor(anotherSubscription)
        feedItemDAO.add(anotherFeedItem);

        val feedItems = feedItemDAO.getChannelFeedItems(channelId = channel.id, 10, 1)

        assertEquals(2, feedItems.totalCount);
    }

    @Test
    fun canFilterChannelFeedItemsBySubscriptions() {
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()

        val subscription = testSubscription(channel)
        val anotherSubscription = testSubscription(channel)
        val yetAnotherSubscription = testSubscription(channel)

        feedItemDAO.add(testFeedItemFor(subscription));
        feedItemDAO.add(testFeedItemFor(anotherSubscription));
        feedItemDAO.add(testFeedItemFor(yetAnotherSubscription));

        val feedItems = feedItemDAO.getChannelFeedItems(channelId = channel.id, 10, 1, subscriptions = listOf(yetAnotherSubscription.id))

        assertEquals(1, feedItems.totalCount);
    }

    private fun testSubscription(channel: Channel): RssSubscription {
        val subscription = RssSubscription()
        subscription.id = UUID.randomUUID().toString()
        subscription.channelId = channel.id
        return subscription
    }

    private fun testFeedItemFor(subscription: RssSubscription): FeedItem {
        val url = "http://localhost/" + UUID.randomUUID().toString();
        val feedItem = FeedItem(UUID.randomUUID().toString(), url, null, DateTime.now().toDate(), null, null, null, subscription.id, subscription.channelId);
        return feedItem
    }
}
