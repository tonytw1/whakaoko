package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.mongodb.MongoException;

@Component
public class SubscriptionsDAO {

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
	
}
