package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.common.collect.Lists;
import com.mongodb.MongoException;

@Component
public class SubscriptionsDAO {
	
	private static Logger log = Logger.getLogger(SubscriptionsDAO.class);
	
	public static final String LATEST_ITEM_DATE_DESCENDING = "-latestItemDate";
	public static final String LAST_READ_ASCENDING = "lastRead";

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
	
	public List<Subscription> getSubscriptions(String order, String url) {
		try {
			Query<Subscription> query = dataStoreFactory.getDs().createQuery(Subscription.class);
			if (Strings.isNullOrEmpty(url)) {
				query = query.filter("url", url);
			}

			List<Subscription> subscriptions = query.order(order).asList();
			log.info("Loaded subscriptions: " + subscriptions.size());
			return subscriptions;
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}

	public Subscription getById(String username, String id) throws UnknownSubscriptionException {
        try {
			final Subscription subscription = dataStoreFactory.getDs().createQuery(Subscription.class).filter("username", username).filter("id", id).get();
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

	public void delete(Subscription subscription) throws UnknownHostException, MongoException {
		log.info("Deleting subscription: " + subscription);
		final Datastore datastore = dataStoreFactory.getDs();
		datastore.delete(datastore.createQuery(Subscription.class).filter("id", subscription.getId()));
	}

	public InstagramSubscription getByInstagramId(Long subscriptionId) throws UnknownHostException, MongoException {
		return dataStoreFactory.getDs().find(InstagramSubscription.class, "subscriptionId", subscriptionId).get();	// TODO subscriptionId is not a very clear field name
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
	
	public List<Subscription> getSubscriptionsForChannel(String username, String channelID) throws UnknownHostException, MongoException {
		return dataStoreFactory.getDs().find(Subscription.class).
			filter("username", username).
			filter("channelId", channelID).		
			order(LATEST_ITEM_DATE_DESCENDING).
			asList();
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
