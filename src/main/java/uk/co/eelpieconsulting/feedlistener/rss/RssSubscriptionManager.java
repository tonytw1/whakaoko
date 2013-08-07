package uk.co.eelpieconsulting.feedlistener.rss;

import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;

public class RssSubscriptionManager {
	
	public RssSubscription requestFeedSubscription(String url, String channel) {
		return new RssSubscription(url, channel);
	}
	
}
