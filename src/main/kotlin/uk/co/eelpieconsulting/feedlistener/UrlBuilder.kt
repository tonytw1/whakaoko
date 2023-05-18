package uk.co.eelpieconsulting.feedlistener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.Subscription

@Component
class UrlBuilder @Autowired constructor(@Value("\${base.url}") private val baseUrl: String) {

    fun getSubscriptionUrl(subscription: Subscription): String {
        return baseUrl + "/ui/subscriptions/" + subscription.id
    }

    fun getSubscriptionUrl(subscriptionId: String): String {
        return "$baseUrl/ui/subscriptions/$subscriptionId"
    }

    fun getDeleteSubscriptionUrl(subscription: Subscription): String {
        return subscriptionUrl(subscription) + "/delete"
    }

    fun getReadSubscriptionUrl(subscription: Subscription): String {
        return "/ui/subscriptions/" + subscription.id + "/read"
    }

    fun getSubscriptionItemsUrl(subscription: Subscription): String {
        return subscriptionUrl(subscription) + "/items"
    }

    fun getChannelItemsUrl(channel: Channel): String {
        return baseUrl + "/channels/" + channel.id + "/items"
    }

    fun getChannelNewSubscriptionUrl(channel: Channel): String {
        return baseUrl + "/ui/subscriptions/" + channel.id + "/new"
    }

    fun getChannelUrl(channel: Channel): String {
        return baseUrl + "/ui/channels/" + channel.id
    }

    val userUrl: String
        get() = "$baseUrl/"
    val newChannelUrl: String
        get() = "$baseUrl/ui/channels/new"

    private fun subscriptionUrl(subscription: Subscription): String {
        return baseUrl + "/subscriptions/" + subscription.id
    }

}
