package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;

@Component
public class RssPoller {
	
	private static Logger log = Logger.getLogger(RssPoller.class);
	
	private final SubscriptionsDAO subscriptionsDAO;
	private final FeedFetcher feedFetcher;
	private final FeedItemDAO feedItemDAO;
	
	@Autowired
	public RssPoller(SubscriptionsDAO subscriptionsDAO, FeedFetcher feedFetcher, FeedItemDAO feedItemDAO) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.feedFetcher = feedFetcher;
		this.feedItemDAO = feedItemDAO;
	}
	
	public void run() {
		log.info("Polling subscription");
		List<RssSubscription> subscriptions = subscriptionsDAO.getSubscriptions();
		for (RssSubscription subscription : subscriptions) {
			log.info("Polling RSS feed: " + subscription.getUrl());
			final FetchedFeed fetchedFeed = feedFetcher.fetchFeed(subscription.getUrl());
			if (fetchedFeed != null) {
				log.info("Fetched feed: " + fetchedFeed.getFeedName());
				for (FeedItem feedItem : fetchedFeed.getFeedItems()) {
					feedItemDAO.add(feedItem);
				}
			} else {
				log.warn("Failed to fetch feed: " + subscription);
			}
		}
		log.info("Done.");
	}

}
