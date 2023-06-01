package uk.co.eelpieconsulting.feedlistener

import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.eelpieconsulting.feedlistener.model.Category
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.rss.classification.FeedStatus
import java.util.*

interface TestData {

    fun testSubscription(channel: Channel, classifications: Set<FeedStatus> = emptySet()): RssSubscription {
        val subscription = RssSubscription(url = "http://localhost/rss", channelId = channel.id, username = "a-user", classifications = classifications)
        subscription.id = UUID.randomUUID().toString()
        subscription.channelId = channel.id
        return subscription
    }

    fun testFeedItemFor(subscription: RssSubscription, categories: List<Category>? = null, date: DateTime = DateTime(2023, 5, 2, 12, 23, DateTimeZone.UTC)): FeedItem {
        val url = "http://localhost/" + UUID.randomUUID().toString()
        return FeedItem(
            ObjectId.get(),
            UUID.randomUUID().toString(),
            url,
            null,
            date.toDate(),
            date.toDate(),
            null,
            null,
            null,
            subscription.id,
            subscription.channelId,
            categories,
            date.toDate()
        )
    }

}