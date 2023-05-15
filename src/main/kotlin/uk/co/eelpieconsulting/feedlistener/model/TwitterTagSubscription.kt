package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity
import org.apache.commons.codec.digest.DigestUtils

@Entity("subscriptions")
class TwitterTagSubscription(val tag: String, channelId: String, username: String) : Subscription() {

    init {
        id = "twitter-" + DigestUtils.md5Hex("tag$tag")
        name = generateName(tag)
        this.channelId = channelId
        this.username = username
    }

    override fun toString(): String {
        return "TwitterTagSubscription [tag=$tag]"
    }

    private fun generateName(tag: String): String {
        return "Twitter - $tag"
    }

}