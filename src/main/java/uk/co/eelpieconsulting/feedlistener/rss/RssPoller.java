package uk.co.eelpieconsulting.feedlistener.rss;

import java.net.UnknownHostException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.mongodb.MongoException;
import com.sun.syndication.io.FeedException;

@Component
public class RssPoller {
	
	private static final Logger log = Logger.getLogger(RssPoller.class);
	
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
		taskExecutor.execute(new ProcessFeedTask(feedFetcher, feedItemDAO, subscriptionsDAO, (RssSubscription) subscription));
	}
	
	private class ProcessFeedTask implements Runnable {
		
		private final FeedFetcher feedFetcher;
		private final FeedItemDAO feedItemDAO;
		private final RssSubscription subscription;
		private final SubscriptionsDAO subscriptionsDAO;
				
		public ProcessFeedTask(FeedFetcher feedFetcher, FeedItemDAO feedItemDAO, SubscriptionsDAO subscriptionsDAO, RssSubscription subscription) {
			this.feedFetcher = feedFetcher;
			this.feedItemDAO = feedItemDAO;
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
				Date latestItemDate = null;
				for (FeedItem feedItem : fetchedFeed.getFeedItems()) {
					try {
						feedItem.setSubscriptionId(subscription.getId());
						feedItemDAO.add(feedItem);

						final Date feedItemDate = feedItem.getDate();
						if (feedItemDate != null && (latestItemDate == null || feedItemDate.after(latestItemDate))) {
							latestItemDate = feedItemDate;
						}

					} catch (UnknownHostException e) {
						log.error(e);
					} catch (MongoException e) {
						log.error(e);
					}
				}

				subscription.setName(fetchedFeed.getFeedName());
				subscription.setLatestItemDate(latestItemDate);
				subscriptionsDAO.save(subscription);
				
				
			} catch (HttpFetchException e) {
				log.error("Http fetch exception while fetching RSS subscription: " + subscription.getName() + ": " + e.getMessage());
			} catch (FeedException e) {
				log.error("Feed exception while parsing RSS subscription: " + subscription.getName() + ": " + e.getMessage());
			}
			// TODO record error condition			
		}
	}
	
	private boolean isRssSubscription(Subscription subscription) {
		return subscription.getId().contains("feed-");	// TODO implement better
	}
	
}
