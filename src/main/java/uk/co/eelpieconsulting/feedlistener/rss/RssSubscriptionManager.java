package uk.co.eelpieconsulting.feedlistener.rss;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class RssSubscriptionManager {
	
	private static Logger log = Logger.getLogger(RssSubscriptionManager.class);
	
	private final SubscriptionsDAO subscriptionsDAO;
	
	@Autowired
	public RssSubscriptionManager(SubscriptionsDAO subscriptionsDAO) {
		this.subscriptionsDAO = subscriptionsDAO;
	}
	
	public Subscription requestFeedSubscription(String url, String channel) {
		log.info("Requesting subscription to feed: " + url);
		final RssSubscription newSubscription = new RssSubscription(url, channel);
		final Subscription existingSubscription = subscriptionsDAO.getById(newSubscription.getId());
		return existingSubscription != null ? existingSubscription : newSubscription;
	}
	
}
