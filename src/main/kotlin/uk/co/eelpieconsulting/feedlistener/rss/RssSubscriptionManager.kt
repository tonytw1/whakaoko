package uk.co.eelpieconsulting.feedlistener.rss

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

@Component
class RssSubscriptionManager @Autowired constructor(
    private val subscriptionsDAO: SubscriptionsDAO,
    private val rssPoller: RssPoller
) {

    private val log = LogManager.getLogger(RssSubscriptionManager::class.java)

    fun requestFeedSubscription(url: String, channel: String, username: String): RssSubscription {
        log.info("Requesting subscription to feed: $url")
        val newSubscription = RssSubscription(url, channel, username)
        log.info("New subscription has idempotency id: ${newSubscription.id}")
        if (subscriptionsDAO.subscriptionExists(newSubscription.id)) {
            val existingSubscription = subscriptionsDAO.getByRssSubscriptionById(newSubscription.id)
            if (existingSubscription != null) {
                log.info("Found existing subscription: ${existingSubscription.id}")
                return existingSubscription
            }
        }

        subscriptionsDAO.add(newSubscription)
        log.info("Added subscription: $newSubscription")
        rssPoller.requestRead(newSubscription)

        return newSubscription
    }

}