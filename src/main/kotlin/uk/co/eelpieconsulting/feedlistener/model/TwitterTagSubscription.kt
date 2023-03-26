package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity
import org.apache.commons.codec.digest.DigestUtils

@Entity("subscriptions")
class TwitterTagSubscription : Subscription {
    var tag: String? = null
        private set

    constructor()
    constructor(tag: String, channel: String?, username: String?) {
        id = "twitter-" + DigestUtils.md5Hex("tag$tag")
        setTag(tag)
        channelId = channel!!
        this.username = username!!
    }

    fun setTag(tag: String) {
        this.tag = tag
        name = generateName(tag)
    }

    override fun toString(): String {
        return "TwitterTagSubscription [tag=$tag]"
    }

    private fun generateName(tag: String): String {
        return "Twitter - $tag"
    }
}