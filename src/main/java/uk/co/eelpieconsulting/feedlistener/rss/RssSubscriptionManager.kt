package uk.co.eelpieconsulting.feedlistener.rss

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

@Component
class RssSubscriptionManager @Autowired constructor(private val subscriptionsDAO: SubscriptionsDAO) {

    private val log = Logger.getLogger(RssSubscriptionManager::class.java)

    fun requestFeedSubscription(url: String, channel: String, username: String): RssSubscription {
        log.info("Requesting subscription to feed: $url")
        val newSubscription = RssSubscription(url, channel, username)
        if (subscriptionsDAO.subscriptionExists(newSubscription.id)) {
            val existingSubscription = subscriptionsDAO.getByRssSubscriptionById(newSubscription.id)
            if (existingSubscription != null) {
                return existingSubscription
            }
        }
        // TODO who should save this
        return newSubscription
    }

}