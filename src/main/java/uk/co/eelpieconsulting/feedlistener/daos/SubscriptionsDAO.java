package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller;

import com.google.code.morphia.Datastore;
import com.google.common.collect.Lists;
import com.mongodb.MongoException;

@Component
public class SubscriptionsDAO {
	
	private static Logger log = Logger.getLogger(RssPoller.class);

	private final DataStoreFactory dataStoreFactory;

	@Autowired
	public SubscriptionsDAO(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}
	
	public synchronized void add(Subscription subscription) {
		for (Subscription existingSubscription : getSubscriptions()) {
			if (existingSubscription.getId().equals(subscription.getId())) {
				return;
			}
		}		
		save(subscription);
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
			return dataStoreFactory.getDatastore().find(Subscription.class).asList();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}

	public Subscription getById(String id) {
        try {
			return dataStoreFactory.getDatastore().find(Subscription.class, "id", id).get();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}

	public void delete(Subscription subscription) throws UnknownHostException, MongoException {
		log.info("Deleting subscription: " + subscription);
		final Datastore datastore = dataStoreFactory.getDatastore();
		datastore.delete(datastore.createQuery(Subscription.class).filter("id", subscription.getId()));
	}

	public InstagramSubscription getByInstagramId(Long subscriptionId) throws UnknownHostException, MongoException {
		return dataStoreFactory.getDatastore().find(InstagramSubscription.class, "subscriptionId", subscriptionId).get();
	}

	public List<Subscription> getTwitterSubscriptions() {
		List<Subscription> subscriptions = Lists.newArrayList();
		for (Subscription subscription : getSubscriptions()) {
			if (subscription.getId().startsWith("twitter")) {
				subscriptions.add(subscription);
			}
		}
		return subscriptions;
	}
	
}
