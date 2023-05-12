package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.mongodb.MongoException
import dev.morphia.DeleteOptions
import dev.morphia.query.FindOptions
import dev.morphia.query.Query
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.annotations.Timed
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import java.util.*
import java.util.regex.Pattern

@Component
class FeedItemDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory) {

    private val log = LogManager.getLogger(FeedItemDAO::class.java)

    private val CHANNEL_ID = "channelId"
    private val SUBSCRIPTION_ID = "subscriptionId"
    private val DATE_DESCENDING_THEN_ID = arrayOf(Sort.descending("date"), Sort.ascending("_id"))
    private val DEFAULT_FEED_ITEMS = 25
    private val MAX_FEED_ITEMS = 100

    fun before(date: Date): MutableList<FeedItem>? {
        val query = dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.lt("date", date));
        return query.iterator(FindOptions().limit(1000).sort(*DATE_DESCENDING_THEN_ID)).toList()
    }

    fun add(feedItem: FeedItem): Boolean {
        return try {
            val existingSubscriptionFeeditemsWithSameUrl = dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq("url", feedItem.url), Filters.eq(SUBSCRIPTION_ID, feedItem.subscriptionId))
            val existing = existingSubscriptionFeeditemsWithSameUrl.first()
            val isNewItem = existing == null
            val replacesItemWithNoDate = feedItem.date != null && existing != null && existing.date == null
            val shouldSave = isNewItem || replacesItemWithNoDate
            // We allow overwriting of the existing feed item with the same url only if
            // the replacement is considered better because it has a date
            log.info("Deciding if to save: " + feedItem + " / " + existing + " / " + shouldSave)
            if (shouldSave) {
                if (existing != null) {
                    feedItem.objectId = existing.objectId
                }
                log.info("Added: " + feedItem.subscriptionId + ", " + feedItem.title)
                dataStoreFactory.get().save(feedItem)
                true

            } else {
                log.debug("Skipping previously added: " + feedItem.title)
                false
            }
        } catch (e: Exception) {
            throw FeeditemPersistanceException(e)
        }
    }

    fun update(feedItem: FeedItem) {
        dataStoreFactory.get().save(feedItem)
    }

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

        return if (!Strings.isNullOrEmpty(q)) searchChannelFeedItems(channel.id, pageSizeToUse, pageToUse, q) else getChannelFeedItems(
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
        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page).sort(*DATE_DESCENDING_THEN_ID)).toList(), totalItems)
    }

    @Throws(MongoException::class)
    fun deleteSubscriptionFeedItems(subscription: Subscription) {
        dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq(SUBSCRIPTION_ID, subscription.id)).delete(DeleteOptions().multi(true))
    }

    @Timed(timingNotes = "")
    fun getSubscriptionFeedItemsCount(subscriptionId: String): Long {
        return subscriptionFeedItemsQuery(subscriptionId).count()
    }

    @Timed(timingNotes = "")
    @Throws(MongoException::class)
    fun getChannelFeedItems(channelId: String, pageSize: Int, page: Int, subscriptions: List<String>? = null): FeedItemsResult {
        val query = channelFeedItemsQuery(channelId, subscriptions)
        val totalCount = query.count()
        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page).sort(*DATE_DESCENDING_THEN_ID)).toList(), totalCount)
    }

    @Timed(timingNotes = "")
    fun searchChannelFeedItems(channelId: String, pageSize: Int, page: Int, q: String?): FeedItemsResult {
        val query = channelFeedItemsQuery(channelId).filter(Filters.eq("title", Pattern.compile(q))) // TODO can eq be used with a patten?
        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page)).toList(), query.count())
    }

    @Timed(timingNotes = "")
    private fun channelFeedItemsQuery(channelId: String, subscriptions: List<String>? = null): Query<FeedItem> {
        val channelFeedItems = dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq(CHANNEL_ID, channelId))
        if (subscriptions != null) {
            return channelFeedItems.filter(Filters.`in`(SUBSCRIPTION_ID, subscriptions))
        }
        return channelFeedItems
    }

    private fun subscriptionFeedItemsQuery(subscriptionId: String): Query<FeedItem> {
        return dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq(SUBSCRIPTION_ID, subscriptionId))
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
        val pageSizeToUse = pageSize ?: DEFAULT_FEED_ITEMS
        if (pageSizeToUse > MAX_FEED_ITEMS) {
            throw RuntimeException("Too many records requested") // TODO use correct exception.
        }
        return pageSizeToUse
    }

}