package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class RssPoller {
	
	private static Logger log = Logger.getLogger(RssPoller.class);
	
	private final SubscriptionsDAO subscriptionsDAO;
	private final FeedFetcher feedFetcher;
	private final FeedItemDAO feedItemDAO;
	private final TaskExecutor taskExecutor;
	
	@Autowired
	public RssPoller(SubscriptionsDAO subscriptionsDAO, FeedFetcher feedFetcher, FeedItemDAO feedItemDAO, TaskExecutor taskExecutor) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.feedFetcher = feedFetcher;
		this.feedItemDAO = feedItemDAO;
		this.taskExecutor = taskExecutor;
	}
	
	@Scheduled(fixedRate=300000)
	public void run() {
		log.info("Polling subscriptions");
		List<Subscription> subscriptions = subscriptionsDAO.getSubscriptions();
		for (Subscription subscription : subscriptions) {
			if (subscription.getId().startsWith("feeds/")) {
				taskExecutor.execute(new ProcessFeedTask(feedFetcher, feedItemDAO, (RssSubscription) subscription));
			}
		}
		log.info("Done.");
	}
	
	private class ProcessFeedTask implements Runnable {
		
		private final FeedFetcher feedFetcher;
		private final FeedItemDAO feedItemDAO;
		private final RssSubscription subscription;
				
		public ProcessFeedTask(FeedFetcher feedFetcher, FeedItemDAO feedItemDAO, RssSubscription subscription) {
			this.feedFetcher = feedFetcher;
			this.feedItemDAO = feedItemDAO;
			this.subscription = subscription;
		}

		public void run() {
			log.info("Processing feed: " + subscription);
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
	}
	
}
