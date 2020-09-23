package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.mongodb.MongoException
import dev.morphia.DeleteOptions
import dev.morphia.query.FindOptions
import dev.morphia.query.Query
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.annotations.Timed
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import java.util.regex.Pattern

@Component
class FeedItemDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory, private val subscriptionsDAO: SubscriptionsDAO) {

    private val log = Logger.getLogger(FeedItemDAO::class.java)

    private val DATE_DESCENDING_THEN_ID = arrayOf(Sort.descending("date"), Sort.ascending("_id"))
    private val MAX_FEED_ITEMS = 25

    fun add(feedItem: FeedItem): Boolean {
        return try {
            if (dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq("url", feedItem.url)).iterator().toList().isEmpty()) {    // TODO shouldn't need to read before every write - use an upsert?
                log.info("Added: " + feedItem.subscriptionId + ", " + feedItem.title)
                dataStoreFactory.get().save(feedItem)
                true
            } else {
                log.info("Skipping previously added: " + feedItem.title)
                false
            }
        } catch (e: Exception) {
            throw FeeditemPersistanceException(e)
        }
    }

    fun addAll(feedItems: List<FeedItem>) {
        for (feedItem in feedItems) {
            add(feedItem)
        }
    }

    fun getSubscriptionFeedItems(subscription: Subscription, page: Int?): FeedItemsResult {
        return if (page != null) {
            getSubscriptionFeedItems(subscription.id, MAX_FEED_ITEMS, page)
        } else {
            getSubscriptionFeedItems(subscription.id, MAX_FEED_ITEMS)
        }
    }

    fun getChannelFeedItemsResult(username: String?, channel: Channel, page: Int?, q: String?, pageSize: Int?): FeedItemsResult {
        val pageSizeToUse = pageSize ?: MAX_FEED_ITEMS
        val pageToUse = if (page != null && page > 0) page else 1
        if (pageSizeToUse > MAX_FEED_ITEMS) {
            throw RuntimeException("Too many records requested") // TODO use correct exception.
        }
        return if (!Strings.isNullOrEmpty(q)) searchChannelFeedItems(channel.id, pageSizeToUse, pageToUse, username, q) else getChannelFeedItems(channel.id, pageSizeToUse, pageToUse, username)
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
        dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq("subscriptionId", subscription.id)).delete(DeleteOptions().multi(true))
    }

    @Timed(timingNotes = "")
    fun getSubscriptionFeedItemsCount(subscriptionId: String): Long {
        return subscriptionFeedItemsQuery(subscriptionId).count()
    }

    @Timed(timingNotes = "")
    @Throws(MongoException::class)
    fun getChannelFeedItems(channelId: String, pageSize: Int, page: Int, username: String?): FeedItemsResult {
        val query = channelFeedItemsQuery(username, channelId)
        val totalCount = query.count()
        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page).sort(*DATE_DESCENDING_THEN_ID)).toList(), totalCount)
    }

    @Timed(timingNotes = "")
    fun searchChannelFeedItems(channelId: String, pageSize: Int, page: Int, username: String?, q: String?): FeedItemsResult {
        val query = channelFeedItemsQuery(username, channelId).filter(Filters.eq("title", Pattern.compile(q))) // TODO can eq be used with a patten?
        return FeedItemsResult(query.iterator(withPaginationFor(pageSize, page)).toList(), query.count())
    }

    @Timed(timingNotes = "")
    private fun channelFeedItemsQuery(username: String?, channelId: String): Query<FeedItem> {
        val channelSubscriptionIds: MutableList<String?> = Lists.newArrayList()
        for (subscription in subscriptionsDAO.getSubscriptionsForChannel(username!!, channelId, null)) {
            channelSubscriptionIds.add(subscription.id)
        }
        return dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.`in`("subscriptionId", channelSubscriptionIds))
    }

    private fun subscriptionFeedItemsQuery(subscriptionId: String): Query<FeedItem> {
        return dataStoreFactory.get().find(FeedItem::class.java).filter(Filters.eq("subscriptionId", subscriptionId))
    }

    private fun withPaginationFor(pageSize: Int, page: Int): FindOptions {
        return FindOptions().limit(pageSize).skip(calculatePageOffset(pageSize, page))
    }

    private fun calculatePageOffset(pageSize: Int, page: Int): Int {
        return if (page > 0) {
            (page - 1) * pageSize
        } else 0
    }

}