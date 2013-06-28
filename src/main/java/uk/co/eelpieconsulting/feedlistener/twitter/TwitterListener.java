package uk.co.eelpieconsulting.feedlistener.twitter;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

@Component
public class TwitterListener {
	
	private static Logger log = Logger.getLogger(TwitterListener.class);
	
	private final SubscriptionsDAO subscriptionsDAO;
	private FeedItemDAO feedItemDAO;
	
	private final String consumerKey;	
	private final String consumerSecret;
	private final String accessToken;
	private final String accessSecret;

	private TwitterStream twitterStream;
	
	@Autowired
	public TwitterListener(SubscriptionsDAO subscriptionsDAO, FeedItemDAO feedItemDAO,
			@Value("#{config['twitter.consumer.key']}") String consumerKey,	
			@Value("#{config['twitter.consumer.secret']}") String consumerSecret,
			@Value("#{config['twitter.access.token']}") String accessToken,
			@Value("#{config['twitter.access.secret']}") String accessSecret) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.feedItemDAO = feedItemDAO;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessSecret = accessSecret;		
		connect();
	}
	
	public void connect() {		
		final StatusListener listener = new StatusListener() {
			
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
				final LatLong latLong = extractLocationFrom(status);				
				final String mediaUrl = extractImageUrl(status);
				return new FeedItem(extractHeadingFrom(status), extractUrlFrom(status), null, status.getCreatedAt(), latLong, mediaUrl);
			}

			private String extractHeadingFrom(Status status) {
				return "@" + status.getUser().getScreenName() + ": " + status.getText();
			}

			private String extractUrlFrom(Status status) {
				return "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + Long.toString(status.getId());
			}

			private LatLong extractLocationFrom(Status status) {
				LatLong latLong = null;
				if (status.getGeoLocation() != null) {
					final GeoLocation geoLocation = status.getGeoLocation();
					latLong = new LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
				}
				return latLong;
			}

			private String extractImageUrl(Status status) {
				if (status.getMediaEntities().length > 0) {
					final MediaEntity mediaEntity = status.getMediaEntities()[0];
					String type = mediaEntity.getType();
					String url = mediaEntity.getMediaURL();
					if (!Strings.isNullOrEmpty(type) && type.equals("photo") && !Strings.isNullOrEmpty(url)) {
						return url;
					}
				}
				return null;
			}			
		};
		
		if (twitterStream != null) {
			twitterStream.cleanUp();
		}
		
		twitterStream = new TwitterApiFactory().getStreamingApi(consumerKey, consumerSecret, accessToken, accessSecret);
		twitterStream.addListener(listener);

		final Set<String> tagsList = Sets.newHashSet();
		for (Subscription subscription : subscriptionsDAO.getSubscriptions()) {
			if (subscription.getId().startsWith("twitter/")) {
				tagsList.add( ((TwitterTagSubscription) subscription).getTag());
			}
		}
		
		if (!tagsList.isEmpty()) {
			final String[] tags = tagsList.toArray(new String[tagsList.size()]);
			twitterStream.filter(new FilterQuery().track(tags));
		}
	}
	
}
