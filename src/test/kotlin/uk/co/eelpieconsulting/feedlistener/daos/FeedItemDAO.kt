package uk.co.eelpieconsulting.feedlistener.daos

import junit.framework.Assert.assertEquals
import org.junit.Test
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class FeedItemDAOTest {

    val mongoDatabase = "whakaokotest" + UUID.randomUUID().toString();

    @Test
    fun canFetchSubscriptionFeedItems() {
        val dataStoreFactory = DataStoreFactory("mongodb://localhost:27017", mongoDatabase);
        val subscriptionsDAO = SubscriptionsDAO(dataStoreFactory);
        val feedItemDAO = FeedItemDAO(dataStoreFactory, subscriptionsDAO)

        val subscription = RssSubscription();
        subscription.id = UUID.randomUUID().toString()

        val url = "http://localhost/" + UUID.randomUUID().toString();
        val feedItem = FeedItem(UUID.randomUUID().toString(), url, null, null, null, null, null);
        feedItem.subscriptionId = subscription.id
        feedItemDAO.add(feedItem);

        val reloaded = feedItemDAO.getSubscriptionFeedItems(subscription, 1);
        assertEquals(1, reloaded.totalCount);
        assertEquals(feedItem.url, reloaded.feedsItems.first().url);
    }

}