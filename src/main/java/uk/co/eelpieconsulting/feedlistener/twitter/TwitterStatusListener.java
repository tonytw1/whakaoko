package uk.co.eelpieconsulting.feedlistener.twitter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

public class TwitterStatusListener implements StatusListener {
	
	private static Logger log = Logger.getLogger(TwitterListener.class);
	
	private final FeedItemDAO feedItemDAO;
	private final TwitterFeedItemMapper twitterFeedItemMapper;
	
	@Autowired
	public TwitterStatusListener(FeedItemDAO feedItemDAO, TwitterFeedItemMapper twitterFeedItemMapper) {
		this.feedItemDAO = feedItemDAO;
		this.twitterFeedItemMapper = twitterFeedItemMapper;
	}

	public void onStatus(Status status) {
		final FeedItem tweetFeedItem = tweetToFeedItem(status);
		log.info("Received: " + tweetFeedItem.getHeadline());
		feedItemDAO.add(tweetFeedItem);
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
