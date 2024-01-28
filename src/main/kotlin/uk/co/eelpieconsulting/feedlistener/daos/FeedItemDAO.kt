package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.mongodb.MongoException
import dev.morphia.DeleteOptions
import dev.morphia.query.FindOptions
import dev.morphia.query.Query
import dev.morphia.query.Sort
import dev.morphia.query.filters.Filters
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import java.util.*
import java.util.regex.Pattern

@Component
class FeedItemDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory) {

    private val log = LogManager.getLogger(FeedItemDAO::class.java)

    private val channelId = "channelId"
    private val subscriptionId = "subscriptionId"
    private val ordering = "ordering"
    private val orderDescendingThenIdAscending = arrayOf(Sort.descending(ordering), Sort.ascending("_id"))
    private val defaultFeedItems = 25
    private val maxFeedItems = 100

    fun before(date: Date): MutableList<FeedItem>? {
        val query = dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.lt("date", date))
        return query.iterator(FindOptions().limit(1000).sort(*orderDescendingThenIdAscending)).toList()
    }

    fun add(feedItem: FeedItem): Boolean {
        return try {
            log.info("Added: " + feedItem.subscriptionId + ", " + feedItem.title)
            dataStoreFactory.get().save(feedItem)
            true
        } catch (e: Exception) {
            log.error("Could not add feed item", e)
            false
        }
    }
    fun getExistingFeedItemByUrlAndSubscription(feedItem: FeedItem): Query<FeedItem> =
        dataStoreFactory.get().find(FeedItem::class.java)
            .filter(Filters.eq("url", feedItem.url), Filters.eq(subscriptionId, feedItem.subscriptionId))

    fun getSubscriptionFeedItems(subscription: Subscription, page: Int?, pageSize: Int? = null): FeedItemsResult {
        val pageSizeToUse = pageSizeToUse(pageSize)

        return if (page != null) {
            getSubscriptionFeedItems(subscription.id, pageSizeToUse, page)
        } else {
            getSubscriptionFeedItems(subscription.id, pageSizeToUse)
        }
    }

    fun getChannelFeedItemsResult(channel: Channel, page: Int?, q: String?, pageSize: Int?, subscriptions: List<String>? = null): FeedItemsResult {
        val pageSizeToUse = pageSizeToUse(pageSize)
        val pageToUse = if (page != null && page > 0) page else 1

        return if (q != null && !Strings.isNullOrEmpty(q)) searchChannelFeedItems(channel.id, pageSizeToUse, pageToUse, q) else getChannelFeedItems(
            channel.id,
            pageSizeToUse,
            pageToUse,
            subscriptions
        )
    }

    @Throws(MongoException::class)
    private fun getSubscriptionFeedItems(subscriptionId: String, pageSize: Int): FeedItemsResult {
        return getSubscriptionFeedItems(subscriptionId, pageSize, 0)
    }

    @Throws(MongoException::class)
    private fun getSubscriptionFeedItems(subscriptionId: String, pageSize: Int, page: Int): FeedItemsResult {
        val query = subscriptionFeedItemsQuery(subscriptionId)
        val totalItems = query.count()
        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page).sort(*orderDescendingThenIdAscending)).toList(), totalItems)
    }

    @Throws(MongoException::class)
    fun deleteSubscriptionFeedItems(subscription: Subscription) {
        dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq(subscriptionId, subscription.id)).delete(DeleteOptions().multi(true))
    }

    fun getSubscriptionFeedItemsCount(subscriptionId: String): Long {
        return subscriptionFeedItemsQuery(subscriptionId).count()
    }

    @Throws(MongoException::class)
    fun getChannelFeedItems(channelId: String, pageSize: Int, page: Int, subscriptions: List<String>? = null): FeedItemsResult {
        val query = channelFeedItemsQuery(channelId, subscriptions)

        // TODO this produces a separate mongo agg call which is slow and unindexed query.count()
        // ie. db.feeditems.aggregate([ { $match: { channelId: "twickenham", className: { $in: [ "uk.co.eelpieconsulting.feedlistener.model.FeedItem" ]}}}, { $group: { _id: 1, n: { $sum: 1 } } } ])
        // if the discrimator clause is removed then the count examines keys and is 4 times faster.
        // ie. db.feeditems.aggregate([ { $match: { channelId: "twickenham"}}, { $group: { _id: 1, n: { $sum: 1 } } } ])
        // Should Morphia really be adding the discriminator clause to the count query?
        val totalCount = query.count()

        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page).sort(*orderDescendingThenIdAscending)).toList(), totalCount)
    }

    private fun searchChannelFeedItems(channelId: String, pageSize: Int, page: Int, q: String): FeedItemsResult {
        val query = channelFeedItemsQuery(channelId).filter(Filters.eq("title", Pattern.compile(q))) // TODO can eq be used with a patten?
        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page)).toList(), query.count())
    }

    private fun channelFeedItemsQuery(channelId: String, subscriptions: List<String>? = null): Query<FeedItem> {
        val channelFeedItems = dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq(this.channelId, channelId))
        if (subscriptions != null) {
            return channelFeedItems.filter(Filters.`in`(subscriptionId, subscriptions))
        }
        return channelFeedItems
    }

    private fun subscriptionFeedItemsQuery(subscriptionId: String): Query<FeedItem> {
        return dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq(this.subscriptionId, subscriptionId))
    }

    private fun withPaginationFor(pageSize: Int, page: Int): FindOptions {
        return FindOptions().limit(pageSize).skip(calculatePageOffset(pageSize, page))
    }

    private fun calculatePageOffset(pageSize: Int, page: Int): Int {
        return if (page > 0) {
            (page - 1) * pageSize
        } else 0
    }

    private fun pageSizeToUse(pageSize: Int?): Int {
        val pageSizeToUse = pageSize ?: defaultFeedItems
        if (pageSizeToUse > maxFeedItems) {
            throw RuntimeException("Too many records requested") // TODO use correct exception.
        }
        return pageSizeToUse
    }

}