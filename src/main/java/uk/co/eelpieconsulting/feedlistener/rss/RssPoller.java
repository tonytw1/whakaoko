package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.persistance.FeedItemDestination;

import com.sun.syndication.io.FeedException;

@Component
public class RssPoller {
	
	private static final Logger log = Logger.getLogger(RssPoller.class);
	
	private final SubscriptionsDAO subscriptionsDAO;
	private final FeedFetcher feedFetcher;
	private final FeedItemDestination feedItemDestination;
	private final TaskExecutor taskExecutor;
	
	@Autowired
	public RssPoller(SubscriptionsDAO subscriptionsDAO, FeedFetcher feedFetcher, FeedItemDestination feedItemDestination, TaskExecutor taskExecutor) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.feedFetcher = feedFetcher;
		this.feedItemDestination = feedItemDestination;
		this.taskExecutor = taskExecutor;
	}
	
	@Scheduled(fixedRate=3600000)
	public void run() {
		log.info("Polling subscriptions");
		for (Subscription subscription : subscriptionsDAO.getSubscriptions()) {
			if (isRssSubscription(subscription)) {
				executeRssPoll(subscription);
			}
		}
		log.info("Done");
	}
	
	public void run(Subscription subscription) {
		log.info("Polling single subscription: " + subscription.getId());
		executeRssPoll(subscription);
		log.info("Done");		
	}
	
	private void executeRssPoll(Subscription subscription) {
		log.info("Executing RSS poll for: " + subscription.getId());
		ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) taskExecutor;
		log.info("Task executor: active:" + threadPoolTaskExecutor.getActiveCount() + ", pool size: " + threadPoolTaskExecutor.getPoolSize());
		taskExecutor.execute(new ProcessFeedTask(feedFetcher, feedItemDestination, subscriptionsDAO, (RssSubscription) subscription));
	}
	
	private class ProcessFeedTask implements Runnable {
		
		private final FeedFetcher feedFetcher;
		private final FeedItemDestination feedItemDestination;
		private final RssSubscription subscription;
		private final SubscriptionsDAO subscriptionsDAO;
				
		public ProcessFeedTask(FeedFetcher feedFetcher, FeedItemDestination feedItemDestination, SubscriptionsDAO subscriptionsDAO, RssSubscription subscription) {
			this.feedFetcher = feedFetcher;
			this.feedItemDestination = feedItemDestination;
			this.subscription = subscription;
			this.subscriptionsDAO = subscriptionsDAO;
		}

		public void run() {
			log.info("Processing feed: " + subscription + " from thread " + Thread.currentThread().getId());
			subscription.setLastRead(DateTime.now().toDate());
			subscriptionsDAO.save(subscription);
			
			try {
				final FetchedFeed fetchedFeed = feedFetcher.fetchFeed(subscription.getUrl());
				log.info("Fetched feed: " + fetchedFeed.getFeedName());
				
				persistFeedItems(fetchedFeed);
				
				subscription.setName(fetchedFeed.getFeedName());
				subscription.setError(null);
				subscription.setLatestItemDate(getLatestItemDate(fetchedFeed.getFeedItems()));
				subscriptionsDAO.save(subscription);				
				
			} catch (HttpFetchException e) {
				log.error("Http fetch exception while fetching RSS subscription: " + subscription.getName() + ": " + e.getMessage());
				subscription.setError("Http fetch: " + e.getMessage());
				subscriptionsDAO.save(subscription);				

			} catch (FeedException e) {
				log.error("Feed exception while parsing RSS subscription: " + subscription.getName() + ": " + e.getMessage());
				subscription.setError("Feed exception: " + e.getMessage());
				subscriptionsDAO.save(subscription);				
			}
			// TODO record error condition			
		}

		private void persistFeedItems(final FetchedFeed fetchedFeed) {
			for (FeedItem feedItem : fetchedFeed.getFeedItems()) {
				try {
					feedItem.setSubscriptionId(subscription.getId());
					feedItemDestination.add(feedItem);
				} catch (FeeditemPersistanceException e) {
					log.error(e);
				}
			}
		}

		private Date getLatestItemDate(List<FeedItem> feedItems) {
			Date latestItemDate = null;
			for (FeedItem feedItem : feedItems) {
				final Date feedItemDate = feedItem.getDate();
				if (feedItemDate != null && (latestItemDate == null || feedItemDate.after(latestItemDate))) {
					latestItemDate = feedItemDate;
				}
			}
			return latestItemDate;
		}
	}
	
	private boolean isRssSubscription(Subscription subscription) {
		return subscription.getId().contains("feed-");	// TODO implement better
	}
	
}
