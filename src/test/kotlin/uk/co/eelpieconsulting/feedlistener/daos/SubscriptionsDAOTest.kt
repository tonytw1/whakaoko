package uk.co.eelpieconsulting.feedlistener.daos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class SubscriptionsDAOTest {

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
        val channel = Channel()
        channel.id = UUID.randomUUID().toString()
        val subscription = testSubscription(channel)
        subscriptionsDAO.add(subscription)

        val channelSubscriptions = subscriptionsDAO.getSubscriptionsForChannel(channel.id, null)

        assertEquals(1, channelSubscriptions.size)
        assertEquals(subscription.id, channelSubscriptions.first().id)
    }

    private fun testSubscription(channel: Channel): RssSubscription {
        val subscription = RssSubscription()
        subscription.id = UUID.randomUUID().toString()
        subscription.channelId = channel.id
        return subscription
    }

}