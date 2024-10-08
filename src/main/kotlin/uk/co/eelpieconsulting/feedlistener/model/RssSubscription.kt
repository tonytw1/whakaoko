package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity
import org.apache.commons.codec.digest.DigestUtils
import uk.co.eelpieconsulting.feedlistener.rss.classification.FeedStatus

@Entity("subscriptions")
class RssSubscription(
    val url: String, channelId: String, username: String, var classification: FeedStatus? = null, classifications: Set<FeedStatus>? = emptySet()
) : Subscription() {

    init {
        val idempotencyId = channelId + "-" + "feed-" + DigestUtils.md5Hex(url)
        this.id = idempotencyId
        this.channelId = channelId
        this.username =username
        this.classifications = classifications
    }

}