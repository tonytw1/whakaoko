package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.mongodb.MongoException
import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.apache.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription
import java.util.stream.Collectors

@Component
class SubscriptionsDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory) {

    private val log = LogManager.getLogger(SubscriptionsDAO::class.java)

    private val LATEST_ITEM_DATE_DESCENDING = Sort.descending("latestItemDate")
    private val LAST_READ_ASCENDING = Sort.ascending("lastRead")

    @Synchronized
    fun add(subscription: Subscription) {
        if (!subscriptionExists(subscription)) {
            log.debug("Saving subscription")
            save(subscription)
        } else {
            log.debug("Not saving duplication subscription")
        }
    }

    fun save(subscription: Subscription) {
        try {
            dataStoreFactory.get().save(subscription)
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getSubscriptions(sort: Sort?, url: String?): List<Subscription> {
        return try {
            var query = dataStoreFactory.get().find(Subscription::class.java)
            if (!Strings.isNullOrEmpty(url)) {
                query = query.disableValidation().filter(Filters.eq("url", url)) // TODO subclasses to helping here Why is validation disabled?
            }
            val subscriptions = query.iterator(FindOptions().sort(sort)).toList()
            log.info("Loaded subscriptions: " + subscriptions.size)
            subscriptions
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getById(id: String?): Subscription? {
        return dataStoreFactory.get().find(Subscription::class.java).filter(Filters.eq("id", id)).first()
    }

    fun getByRssSubscriptionById(id: String?): RssSubscription? {
        return dataStoreFactory.get().find(RssSubscription::class.java).filter(Filters.eq("id", id)).first()
    }
    fun subscriptionExists(id: String?): Boolean {
        return getById(id) != null
    }

    @Throws(MongoException::class)
    fun delete(subscription: Subscription) {
        log.info("Deleting subscription: $subscription")
        dataStoreFactory.get().find(Subscription::class.java).filter(Filters.eq("id", subscription.id)).delete()
    }

    @Throws(MongoException::class)
    fun getByInstagramId(subscriptionId: Long?): InstagramSubscription {
        return dataStoreFactory.get().find(InstagramSubscription::class.java).filter(Filters.eq("subscriptionId", subscriptionId)).first() // TODO subscriptionId is not a very clear field name
    }

    fun twitterSubscriptions(): List<Subscription> {
        val subscriptions: MutableList<Subscription> = Lists.newArrayList()
        for (subscription in getSubscriptions(LATEST_ITEM_DATE_DESCENDING, null)) {
            if (subscription.id.startsWith("twitter")) {
                subscriptions.add(subscription)
            }
        }
        return subscriptions
    }

    fun getTwitterSubscriptionsFor(username: String): List<TwitterTagSubscription> {
        val allTwitterSubscriptions = dataStoreFactory.get().find(TwitterTagSubscription::class.java).iterator().toList()
        return allTwitterSubscriptions.stream().filter { subscription: TwitterTagSubscription -> username == subscription.username }.collect(Collectors.toList())
    }

    @Throws(MongoException::class)
    fun getSubscriptionsForChannel(channelID: String, url: String?): List<Subscription> {
        log.info("Listing subscriptions for channel: $channelID")
        var query = dataStoreFactory.get().find(Subscription::class.java).filter(Filters.eq("channelId", channelID))
        if (!Strings.isNullOrEmpty(url)) {
            query = query.disableValidation().filter(Filters.eq("url", url)) // TODO subclasses to helping here
        }
        return query.iterator(FindOptions().sort(LATEST_ITEM_DATE_DESCENDING)).toList()
    }

    // TODO optimise for last read ordering
    fun allRssSubscriptions(): List<RssSubscription> {
        val allRssSubscriptions = dataStoreFactory.get().find(RssSubscription::class.java)
        return allRssSubscriptions.iterator().toList() // TODO optimise for last read ordering
    }

    private fun subscriptionExists(subscription: Subscription): Boolean {
        for (existingSubscription in getSubscriptions(LATEST_ITEM_DATE_DESCENDING, null)) {
            if (existingSubscription.id == subscription.id) {
                log.debug("Subscription exists: $subscription")
                return true
            }
        }
        return false
    }

    fun allSubscriptions(): List<Subscription> = dataStoreFactory.get().find(Subscription::class.java).iterator().toList()

}