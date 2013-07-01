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
		final FeedItem tweetFeedItem = tweetToFeedItem(status);
		log.info("Received: " + tweetFeedItem.getHeadline());
		
		final List<Subscription> twitterSubscriptions = subscriptionsDAO.getTwitterSubscriptions();
		for (Subscription subscription : twitterSubscriptions) {
			if (status.getText().toLowerCase().contains(((TwitterTagSubscription) subscription).getTag().toLowerCase())) {
				tweetFeedItem.setSubscriptionId(subscription.getId());
			}
		}
		
		try {
			feedItemDAO.add(tweetFeedItem);
		} catch (UnknownHostException e) {
			log.error(e);
		} catch (MongoException e) {
			log.error(e);
		}
	}
	
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		// TODO implement
	}
	
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		log.warn("Limitation notice; numberOfLimitedStatuses: " + numberOfLimitedStatuses);
	}

	public void onException(Exception e) {
		log.error(e);
	}

	@Override
	public void onScrubGeo(long arg0, long arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStallWarning(StallWarning arg0) {
		// TODO Auto-generated method stub
	}
	
	private FeedItem tweetToFeedItem(Status status) {
		return twitterFeedItemMapper.createFeedItemFrom(status);
	}

}
