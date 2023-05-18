package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.mongodb.MongoException
import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.Subscription

@Component
class SubscriptionsDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory) {

    private val log = LogManager.getLogger(SubscriptionsDAO::class.java)

    private val LATEST_ITEM_DATE_DESCENDING = Sort.descending("latestItemDate")

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

    private fun getSubscriptions(sort: Sort?, url: String?): List<Subscription> {
        return try {
            var query = dataStoreFactory.get().find(Subscription::class.java)
            if (!Strings.isNullOrEmpty(url)) {
                query = query.disableValidation()
                    .filter(Filters.eq("url", url)) // TODO subclasses to helping here Why is validation disabled?
            }
            query.iterator(FindOptions().sort(sort)).toList()
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
    fun getSubscriptionsForChannel(channelID: String, url: String?): List<Subscription> {
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

}