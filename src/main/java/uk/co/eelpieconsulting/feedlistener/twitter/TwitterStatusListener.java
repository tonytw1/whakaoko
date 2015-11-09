package uk.co.eelpieconsulting.feedlistener.twitter;

import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;
import uk.co.eelpieconsulting.feedlistener.persistance.FeedItemDestination;

import com.google.common.collect.Lists;

public class TwitterStatusListener implements StatusListener {
	
	private final static Logger log = Logger.getLogger(TwitterListener.class);
	
	private final FeedItemDestination feedItemDestination;
	private final TwitterFeedItemMapper twitterFeedItemMapper;
	private final SubscriptionsDAO subscriptionsDAO;
	private final String username;
	
	public TwitterStatusListener(FeedItemDestination feedItemDestination, TwitterFeedItemMapper twitterFeedItemMapper, SubscriptionsDAO subscriptionsDAO, String username) {
		this.feedItemDestination = feedItemDestination;
		this.twitterFeedItemMapper = twitterFeedItemMapper;
		this.subscriptionsDAO = subscriptionsDAO;
		this.username = username;
	}

	public void onStatus(Status status) {
		log.info("Received: " + status.getText());
		
		final List<Subscription> subscriptionsMatchingThisTweet = filterSubscriptionsMatchingThisTweet(subscriptionsDAO.getTwitterSubscriptionsFor(username), status);		
		for (Subscription subscription : subscriptionsMatchingThisTweet) {
			final FeedItem tweetFeedItem = twitterFeedItemMapper.createFeedItemFrom(status);
			tweetFeedItem.setSubscriptionId(subscription.getId());	// TODO should we be duplicating tweets like this?
			subscription.setLatestItemDate(status.getCreatedAt());
			subscriptionsDAO.save(subscription);
			
			try {
				feedItemDestination.add(tweetFeedItem);
			} catch (FeeditemPersistanceException e) {
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