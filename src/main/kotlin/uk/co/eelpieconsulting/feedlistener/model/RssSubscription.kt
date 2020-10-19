package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity
import org.apache.commons.codec.digest.DigestUtils

@Entity("subscriptions")
class RssSubscription : Subscription {

    lateinit var url: String
    lateinit var classification: String

    constructor() {}
    constructor(url: String, channelId: String, username: String) {
        id = channelId + "-" + "feed-" + DigestUtils.md5Hex(url)
        this.channelId = channelId
        this.username = username
        this.url = url
    }

}