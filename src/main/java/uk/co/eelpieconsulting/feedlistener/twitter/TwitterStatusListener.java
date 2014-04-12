package uk.co.eelpieconsulting.feedlistener.twitter;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

import com.google.common.collect.Lists;
import com.mongodb.MongoException;

@Component
public class TwitterStatusListener implements StatusListener {
	
	private static Logger log = Logger.getLogger(TwitterListener.class);
	
	private final FeedItemDAO feedItemDAO;
	private final TwitterFeedItemMapper twitterFeedItemMapper;
	private final SubscriptionsDAO subscriptionsDAO;
	
	@Autowired
	public TwitterStatusListener(FeedItemDAO feedItemDAO, TwitterFeedItemMapper twitterFeedItemMapper, SubscriptionsDAO subscriptionsDAO) {
		this.feedItemDAO = feedItemDAO;
		this.twitterFeedItemMapper = twitterFeedItemMapper;
		this.subscriptionsDAO = subscriptionsDAO;
	}

	public void onStatus(Status status) {
		log.info("Received: " + status.getText());
		
		final List<Subscription> subscriptionsMatchingThisTweet = filterSubscriptionsMatchingThisTweet(subscriptionsDAO.getTwitterSubscriptions(), status);		
		for (Subscription subscription : subscriptionsMatchingThisTweet) {
			final FeedItem tweetFeedItem = twitterFeedItemMapper.createFeedItemFrom(status);
			tweetFeedItem.setSubscriptionId(subscription.getId());	// TODO should we be duplicating tweets like this?
			subscription.setLatestItemDate(status.getCreatedAt());
			subscriptionsDAO.save(subscription);
			
			try {
				feedItemDAO.add(tweetFeedItem);
			} catch (UnknownHostException e) {
				log.error(e);
			} catch (MongoException e) {
				log.error(e);
			}			
		}	
	}
	
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		log.warn("Unimplemented deletion notice action for tweet: " + statusDeletionNotice.getStatusId());			// TODO implement
	}
	
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		log.warn("Limitation notice; numberOfLimitedStatuses: " + numberOfLimitedStatuses);
	}

	public void onException(Exception e) {
		log.error(e);
	}

	@Override
	public void onScrubGeo(long arg0, long arg1) {
		log.warn("Unimplemented scrub geo for: " + arg0 + ", " + arg1);	// TODO implement
	}
	
	@Override
	public void onStallWarning(StallWarning stallWarning) {
		log.warn("Unimplemented stall warning: " + stallWarning.getMessage());	// TODO implement
	}
	
	private List<Subscription> filterSubscriptionsMatchingThisTweet(List<Subscription> twitterSubscriptions, Status status) {
		final List<Subscription> filteredSubscriptions = Lists.newArrayList();
		for (Subscription subscription : twitterSubscriptions) {
			if (status.getText().toLowerCase().contains(((TwitterTagSubscription) subscription).getTag().toLowerCase())) {
				filteredSubscriptions.add(subscription);
			}
		}
		return filteredSubscriptions;
	}
	
}
