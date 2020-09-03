package uk.co.eelpieconsulting.feedlistener.twitter;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

import java.util.List;
import java.util.stream.Collectors;

public class TwitterStatusListener implements StatusListener {
	
	private final static Logger log = Logger.getLogger(TwitterListener.class);

	private final FeedItemDAO feedItemDestination;
	private final TwitterFeedItemMapper twitterFeedItemMapper;
	private final SubscriptionsDAO subscriptionsDAO;	// TODO subscriptions dao is an odd pass in; list of subscriptions would be better?
	private final String username;
	
	public TwitterStatusListener(FeedItemDAO feedItemDestination, TwitterFeedItemMapper twitterFeedItemMapper, SubscriptionsDAO subscriptionsDAO, String username) {
		this.feedItemDestination = feedItemDestination;
		this.twitterFeedItemMapper = twitterFeedItemMapper;
		this.subscriptionsDAO = subscriptionsDAO;
		this.username = username;
	}

	public void onStatus(Status status) {
		log.info("Received: " + status.getText());

		List<TwitterTagSubscription> usersTwitterSubscriptions = subscriptionsDAO.getTwitterSubscriptionsFor(username);	// TODO dao hint in each tweet =(
		final List<Subscription> subscriptionsMatchingThisTweet = filterSubscriptionsMatchingThisTweet(usersTwitterSubscriptions, status);
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
	
	private List<Subscription> filterSubscriptionsMatchingThisTweet(List<TwitterTagSubscription> twitterSubscriptions, Status status) {
		return twitterSubscriptions.stream().filter(subscription ->
			status.getText().toLowerCase().contains(subscription.getTag().toLowerCase())
		).collect(Collectors.toList());
	}
	
}