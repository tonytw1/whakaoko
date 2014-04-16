package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.code.morphia.Datastore;
import com.google.common.collect.Lists;
import com.mongodb.MongoException;

@Component
public class SubscriptionsDAO {
	
	private static Logger log = Logger.getLogger(SubscriptionsDAO.class);
	
	private static final String LATEST_ITEM_DATE = "-latestItemDate";
	
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
			dataStoreFactory.getDatastore().save(subscription);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<Subscription> getSubscriptions() {
		try {
			return dataStoreFactory.getDatastore().find(Subscription.class).
				order(LATEST_ITEM_DATE).
				asList();
			
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}

	public Subscription getById(String username, String id) throws UnknownSubscriptionException {
        try {
			final Subscription subscription = dataStoreFactory.getDatastore().createQuery(Subscription.class).filter("username", username).filter("id", id).get();
			if (subscription == null) {
				throw new UnknownSubscriptionException();
			}
			return subscription;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
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
		final Datastore datastore = dataStoreFactory.getDatastore();
		datastore.delete(datastore.createQuery(Subscription.class).filter("id", subscription.getId()));
	}

	public InstagramSubscription getByInstagramId(Long subscriptionId) throws UnknownHostException, MongoException {
		return dataStoreFactory.getDatastore().find(InstagramSubscription.class, "subscriptionId", subscriptionId).get();	// TODO subscriptionId is not a very clear field name
	}

	public List<Subscription> getTwitterSubscriptions() {
		final List<Subscription> subscriptions = Lists.newArrayList();
		for (Subscription subscription : getSubscriptions()) {
			if (subscription.getId().startsWith("twitter")) {
				subscriptions.add(subscription);
			}
		}
		return subscriptions;
	}
	
	public List<Subscription> getTwitterSubscriptionsFor(String username) {
		final List<Subscription> subscriptions = Lists.newArrayList();
		for (Subscription subscription : getSubscriptions()) {
			if (subscription.getId().startsWith("twitter") && username.equals(subscription.getUsername())) {
				subscriptions.add(subscription);
			}
		}
		return subscriptions;
	}
	
	public List<Subscription> getSubscriptionsForChannel(String channelID) throws UnknownHostException, MongoException {
		return dataStoreFactory.getDatastore().find(Subscription.class, "channelId", channelID).
			order(LATEST_ITEM_DATE).
			asList();
	}
	
	private boolean subscriptionExists(Subscription subscription) {
		for (Subscription existingSubscription : getSubscriptions()) {
			if (existingSubscription.getId().equals(subscription.getId())) {
				log.debug("Subscription exists: " + subscription);
				return true;
			}
		}		
		return false;
	}
	
}
