package uk.co.eelpieconsulting.feedlistener.rss;

import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;

@Component
public class RssSubscriptionManager {
	
	public RssSubscription requestFeedSubscription(String url, String channel) {
		return new RssSubscription(url, channel);
	}
	
}
