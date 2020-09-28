package uk.co.eelpieconsulting.feedlistener.daos

import junit.framework.Assert.assertEquals
import org.joda.time.DateTime
import org.junit.Test
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class FeedItemDAOTest {

    val mongoDatabase = "whakaokotest" + UUID.randomUUID().toString();

    val dataStoreFactory = DataStoreFactory("mongodb://localhost:27017", mongoDatabase);
    val subscriptionsDAO = SubscriptionsDAO(dataStoreFactory);
    val feedItemDAO = FeedItemDAO(dataStoreFactory, subscriptionsDAO)
    val channelsDAO = ChannelsDAO(dataStoreFactory)

    @Test
    fun canFetchSubscriptionFeedItems() {
            val subscription = testSubscription()
            subscriptionsDAO.add(subscription);

            val feedItem = testFeedItemFor(subscription)
            feedItemDAO.add(feedItem);

            val reloaded = feedItemDAO.getSubscriptionFeedItems(subscription, 1);
            assertEquals(1, reloaded.totalCount);
            assertEquals(feedItem.url, reloaded.feedsItems.first().url);

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

        val reloaded = feedItemDAO.getChannelFeedItems(channelId = channel.id!!, 10, 1);
        assertEquals(2, reloaded.totalCount);
    }

    private fun testSubscription(channel: Channel? = null): RssSubscription {
        val subscription = RssSubscription();
        subscription.id = UUID.randomUUID().toString()
        subscription.channelId = channel?.id
        return subscription
    }

    private fun testFeedItemFor(subscription: RssSubscription): FeedItem {
        val url = "http://localhost/" + UUID.randomUUID().toString();
        val feedItem = FeedItem(UUID.randomUUID().toString(), url, null, DateTime.now().toDate(), null, null, null, subscription.id, subscription.channelId);
        return feedItem
    }

}