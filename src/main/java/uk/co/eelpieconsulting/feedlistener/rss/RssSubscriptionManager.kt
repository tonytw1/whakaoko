package uk.co.eelpieconsulting.feedlistener.rss;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class RssSubscriptionManager {
	
	private static final Logger log = Logger.getLogger(RssSubscriptionManager.class);
	
	private final SubscriptionsDAO subscriptionsDAO;
	
	@Autowired
	public RssSubscriptionManager(SubscriptionsDAO subscriptionsDAO) {
		this.subscriptionsDAO = subscriptionsDAO;
	}
	
	public RssSubscription requestFeedSubscription(String url, String channel, String username) {
		log.info("Requesting subscription to feed: " + url);
		final RssSubscription newSubscription = new RssSubscription(url, channel, username);
		if (subscriptionsDAO.subscriptionExists(newSubscription.getId())) {
			RssSubscription existingSubscription = subscriptionsDAO.getByRssSubscriptionById(newSubscription.getId());
			if (existingSubscription != null) {
				return existingSubscription;
			}
		}
		return newSubscription;
	}
	
}
