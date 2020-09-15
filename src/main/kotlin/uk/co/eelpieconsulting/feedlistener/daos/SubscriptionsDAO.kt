package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.mongodb.MongoException
import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription
import java.util.stream.Collectors

@Component
class SubscriptionsDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory) {

    private val log = Logger.getLogger(SubscriptionsDAO::class.java)

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
            dataStoreFactory.ds.save(subscription)
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getSubscriptions(sort: Sort?, url: String?): List<Subscription> {
        return try {
            var query = dataStoreFactory.ds.find(Subscription::class.java)
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

    @Throws(UnknownSubscriptionException::class)
    fun getById(id: String?): Subscription {
        return try {
            val subscription = dataStoreFactory.ds.find(Subscription::class.java).filter(Filters.eq("id", id)).first()
            if (subscription == null) {
                log.info("Subscription not found")
                throw UnknownSubscriptionException()
            }
            subscription
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun subscriptionExists(id: String?): Boolean {
        return try {
            getById(id)
            true
        } catch (e: UnknownSubscriptionException) {
            false
        }
    }

    @Throws(MongoException::class)
    fun delete(subscription: Subscription) {
        log.info("Deleting subscription: $subscription")
        val datastore = dataStoreFactory.ds
        datastore.find(Subscription::class.java).filter(Filters.eq("id", subscription.id)).delete()
    }

    @Throws(MongoException::class)
    fun getByInstagramId(subscriptionId: Long?): InstagramSubscription {
        return dataStoreFactory.ds.find(InstagramSubscription::class.java).filter(Filters.eq("subscriptionId", subscriptionId)).first() // TODO subscriptionId is not a very clear field name
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
        val allTwitterSubscriptions = dataStoreFactory.ds.find(TwitterTagSubscription::class.java).iterator().toList()
        return allTwitterSubscriptions.stream().filter { subscription: TwitterTagSubscription -> username == subscription.username }.collect(Collectors.toList())
    }

    @Throws(MongoException::class)
    fun getSubscriptionsForChannel(username: String, channelID: String, url: String?): List<Subscription> {
        log.info("Listing subscriptions for channel: $username / $channelID")
        var query = dataStoreFactory.ds.find(Subscription::class.java).filter(Filters.eq("username", username), Filters.eq("channelId", channelID))
        if (!Strings.isNullOrEmpty(url)) {
            query = query.disableValidation().filter(Filters.eq("url", url)) // TODO subclasses to helping here
        }
        return query.iterator(FindOptions().sort(LATEST_ITEM_DATE_DESCENDING)).toList()
    }

    // TODO optimise for last read ordering
    fun allRssSubscriptions(): List<RssSubscription> {
        val allRssSubscriptions = dataStoreFactory.ds.find(RssSubscription::class.java)
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

    fun allSubscriptions(): List<Subscription> = dataStoreFactory.ds.find(Subscription::class.java).iterator().toList()

    companion object {
        val LATEST_ITEM_DATE_DESCENDING = Sort.descending("latestItemDate")
        val LAST_READ_ASCENDING = Sort.ascending("lastRead")
    }
}