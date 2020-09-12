package uk.co.eelpieconsulting.feedlistener.rss;

import com.sun.syndication.io.FeedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;

import java.util.Date;
import java.util.List;

@Component
public class RssPoller {

    private static final Logger log = Logger.getLogger(RssPoller.class);

    private final SubscriptionsDAO subscriptionsDAO;
    private final FeedFetcher feedFetcher;
    private final FeedItemDAO feedItemDestination;
    private final TaskExecutor taskExecutor;

    private final Counter rssAddedItems;

    @Autowired
    public RssPoller(SubscriptionsDAO subscriptionsDAO, FeedFetcher feedFetcher, FeedItemDAO feedItemDestination,
                     TaskExecutor taskExecutor, MeterRegistry meterRegistry) {
        this.subscriptionsDAO = subscriptionsDAO;
        this.feedFetcher = feedFetcher;
        this.feedItemDestination = feedItemDestination;
        this.taskExecutor = taskExecutor;

        rssAddedItems = meterRegistry.counter("rss_added_items");
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 300000)
    public void run() {
        log.info("Polling subscriptions");
        subscriptionsDAO.getAllRssSubscriptions().forEach(this::executeRssPoll);
        log.info("Done");
    }

    public void run(RssSubscription subscription) {
        log.info("Polling single subscription: " + subscription.getId());
        executeRssPoll(subscription);
        log.info("Done");
    }

    private void executeRssPoll(RssSubscription subscription) {
        log.info("Executing RSS poll for: " + subscription.getId());
        ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) taskExecutor;
        log.info("Task executor: active:" + threadPoolTaskExecutor.getActiveCount() + ", pool size: " + threadPoolTaskExecutor.getPoolSize());
        taskExecutor.execute(new ProcessFeedTask(feedFetcher, feedItemDestination, subscriptionsDAO, subscription));
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
                log.info("Etag: " + fetchedFeed.getEtag());

                persistFeedItems(fetchedFeed);

                subscription.setName(fetchedFeed.getFeedName());
                subscription.setError(null);
                subscription.setEtag(fetchedFeed.getEtag());
                subscription.setLatestItemDate(getLatestItemDate(fetchedFeed.getFeedItems()));
                subscriptionsDAO.save(subscription);

            } catch (HttpFetchException e) {
                log.warn("Http fetch exception while fetching RSS subscription: " + subscription.getUrl() + ": " + e.getClass().getSimpleName());
                subscription.setError("Http fetch: " + e.getMessage());
                subscriptionsDAO.save(subscription);

            } catch (FeedException e) {
                log.warn("Feed exception while parsing RSS subscription: " + subscription.getUrl() + ": " + e.getMessage());
                subscription.setError("Feed exception: " + e.getMessage());
                subscriptionsDAO.save(subscription);

            } catch (Exception e) {
                log.error("Unexpected error while processing feed: " + subscription.getUrl() + ": " + e.getMessage());
                subscription.setError("Feed exception: " + e.getMessage());
                subscriptionsDAO.save(subscription);
            }
        }

        private void persistFeedItems(final FetchedFeed fetchedFeed) {
            for (FeedItem feedItem : fetchedFeed.getFeedItems()) {
                try {
                    feedItem.setSubscriptionId(subscription.getId());
                    if (feedItemDAO.add(feedItem)) {
                        rssAddedItems.increment();
                    }

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

}
