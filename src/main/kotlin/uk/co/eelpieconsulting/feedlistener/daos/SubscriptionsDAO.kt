package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.mongodb.MongoException
import dev.morphia.DeleteOptions
import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.filters.Filters
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.Subscription

@Component
class SubscriptionsDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory) {

    private val log = LogManager.getLogger(SubscriptionsDAO::class.java)

    private val latestItemDateDescending = Sort.descending("latestItemDate")

    @Synchronized
    fun add(subscription: Subscription): Subscription {
        log.info("Adding subscription with id: ${subscription.id}")
        // Look up existing by idempotency id
        val existing = getById(subscription.id)
        if (existing == null) {
            log.info("Saving new subscription")
            return save(subscription)
        } else {
            log.warn("Not saving duplication subscription")
            return existing
        }
    }

    fun save(subscription: Subscription): Subscription {
        try {
            return dataStoreFactory.get().save(subscription)
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    private fun getSubscriptions(sort: Sort?, url: String?): List<Subscription> {
        return try {
            var query = dataStoreFactory.get().find(RssSubscription::class.java)
            if (!Strings.isNullOrEmpty(url)) {
                query = query.disableValidation()
                    .filter(Filters.eq("url", url)) // TODO subclasses to helping here Why is validation disabled?
            }
            query.iterator(FindOptions().sort(sort)).toList()
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getById(id: String): Subscription? {
        log.info("Getting subscription by id: $id")
        return dataStoreFactory.get().find(RssSubscription::class.java).filter(Filters.eq("id", id)).first()
    }

    fun getByRssSubscriptionById(id: String): RssSubscription? {
        return dataStoreFactory.get().find(RssSubscription::class.java).filter(Filters.eq("id", id)).first()
    }
    fun subscriptionExists(id: String): Boolean {
        return getById(id) != null
    }

    @Throws(MongoException::class)
    fun delete(subscription: Subscription) {
        log.info("Deleting subscription: ${subscription.id}")
        val deletedCount =
            dataStoreFactory.get().find(RssSubscription::class.java).filter(Filters.eq("id", subscription.id)).delete(
                DeleteOptions().multi(false)
            ).deletedCount
        log.info("Deleted $deletedCount subscriptions")
    }

    @Throws(MongoException::class)
    fun getSubscriptionsForChannel(channelID: String, url: String?): List<RssSubscription> {
        var query = dataStoreFactory.get().find(RssSubscription::class.java).filter(Filters.eq("channelId", channelID))
        if (!Strings.isNullOrEmpty(url)) {
            query = query.disableValidation().filter(Filters.eq("url", url)) // TODO subclasses to helping here
        }
        return query.iterator(FindOptions().sort(latestItemDateDescending)).toList()
    }

    // TODO optimise for last read ordering
    fun allRssSubscriptions(): List<RssSubscription> {
        val allRssSubscriptions = dataStoreFactory.get().find(RssSubscription::class.java)
        return allRssSubscriptions.iterator().toList() // TODO optimise for last read ordering
    }

}