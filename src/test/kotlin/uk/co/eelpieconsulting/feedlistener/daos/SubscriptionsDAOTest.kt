package uk.co.eelpieconsulting.feedlistener.daos

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.TestData
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.rss.classification.FeedStatus
import java.util.*

class SubscriptionsDAOTest : TestData {

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

    @Test
    fun canFetchSubscriptionByChannel() {
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
        val subscription = testSubscription(channel, classifications = setOf(FeedStatus.frequent, FeedStatus.ok))
        subscriptionsDAO.add(subscription)

        val channelSubscriptions = subscriptionsDAO.getSubscriptionsForChannel(channel.id, null)

        assertEquals(1, channelSubscriptions.size)
        val first = channelSubscriptions.first()
        assertEquals(subscription.id, first.id)
        assertEquals(setOf(FeedStatus.frequent, FeedStatus.ok), first.classifications)
    }

    @Test
    fun canDeleteSubscription() {
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription)

        subscriptionsDAO.delete(subscription)

        assertNull(subscriptionsDAO.getById(subscription.id))
    }



}
