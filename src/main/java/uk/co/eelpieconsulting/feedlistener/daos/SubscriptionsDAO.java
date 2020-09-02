package uk.co.eelpieconsulting.feedlistener.daos;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoException;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import java.util.List;

@Component
public class SubscriptionsDAO {

    private final static Logger log = Logger.getLogger(SubscriptionsDAO.class);

    public static final Sort LATEST_ITEM_DATE_DESCENDING = Sort.descending("latestItemDate");
    public static final Sort LAST_READ_ASCENDING = Sort.ascending("lastRead");

    private final DataStoreFactory dataStoreFactory;

    @Autowired
    public SubscriptionsDAO(DataStoreFactory dataStoreFactory) {
        this.dataStoreFactory = dataStoreFactory;
    }

    public synchronized void add(Subscription subscription) {
        if (!subscriptionExists(subscription)) {
            log.debug("Saving subscription");
            save(subscription);
        } else {
            log.debug("Not saving duplication subscription");
        }
    }

    public void save(Subscription subscription) {
        try {
            dataStoreFactory.getDs().save(subscription);
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Subscription> getSubscriptions(Sort sort, String url) {
        try {
            Query<Subscription> query = dataStoreFactory.getDs().find(Subscription.class);
            if (!Strings.isNullOrEmpty(url)) {
                query = query.disableValidation().filter(Filters.eq("url", url));    // TODO subclasses to helping here Why is validation disabled?
            }

            List<Subscription> subscriptions = query.iterator(new FindOptions().sort(sort)).toList();
            log.info("Loaded subscriptions: " + subscriptions.size());
            return subscriptions;
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    public Subscription getById(String username, String id) throws UnknownSubscriptionException {
        try {
            final Subscription subscription = dataStoreFactory.getDs().find(Subscription.class).filter(Filters.eq("username", username), Filters.eq("id", id)).first();
            if (subscription == null) {
                throw new UnknownSubscriptionException();
            }
            return subscription;
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean subscriptionExists(String username, String id) {
        try {
            getById(username, id);
            return true;
        } catch (UnknownSubscriptionException e) {
            return false;
        }
    }

    public void delete(Subscription subscription) throws MongoException {
        log.info("Deleting subscription: " + subscription);
        final Datastore datastore = dataStoreFactory.getDs();
        datastore.find(Subscription.class).filter(Filters.eq("id", subscription.getId())).first();
    }

    public InstagramSubscription getByInstagramId(Long subscriptionId) throws MongoException {
        return dataStoreFactory.getDs().find(InstagramSubscription.class).filter(Filters.eq("subscriptionId", subscriptionId)).first();    // TODO subscriptionId is not a very clear field name
    }

    public List<Subscription> getTwitterSubscriptions() {
        final List<Subscription> subscriptions = Lists.newArrayList();
        for (Subscription subscription : getSubscriptions(SubscriptionsDAO.LATEST_ITEM_DATE_DESCENDING, null)) {
            if (subscription.getId().startsWith("twitter")) {
                subscriptions.add(subscription);
            }
        }
        return subscriptions;
    }

    public List<Subscription> getTwitterSubscriptionsFor(String username) {
        final List<Subscription> subscriptions = Lists.newArrayList();
        for (Subscription subscription : getSubscriptions(SubscriptionsDAO.LATEST_ITEM_DATE_DESCENDING, null)) {
            if (subscription.getId().startsWith("twitter") && username.equals(subscription.getUsername())) {
                subscriptions.add(subscription);
            }
        }
        return subscriptions;
    }

    public List<Subscription> getSubscriptionsForChannel(String username, String channelID, String url) throws MongoException {
        Query<Subscription> query = dataStoreFactory.getDs().find(Subscription.class).
                filter(Filters.eq("username", username), Filters.eq("channelId", channelID));

        if (!Strings.isNullOrEmpty(url)) {
            query = query.disableValidation().filter(Filters.eq("url", url));    // TODO subclasses to helping here
        }
        return query.iterator(new FindOptions().sort(LATEST_ITEM_DATE_DESCENDING)).toList();
    }

    private boolean subscriptionExists(Subscription subscription) {
        for (Subscription existingSubscription : getSubscriptions(LATEST_ITEM_DATE_DESCENDING, null)) {
            if (existingSubscription.getId().equals(subscription.getId())) {
                log.debug("Subscription exists: " + subscription);
                return true;
            }
        }
        return false;
    }

}
