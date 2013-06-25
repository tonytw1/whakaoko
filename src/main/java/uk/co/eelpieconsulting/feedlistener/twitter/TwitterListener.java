package uk.co.eelpieconsulting.feedlistener.twitter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.common.base.Strings;

@Component
public class TwitterListener {
	
	private static Logger log = Logger.getLogger(TwitterListener.class);
	
	private FeedItemDAO feedItemDAO;
	
	String consumerKey = "";
	String consumerSecret = "";

	String accessToken = "";
	String accessSecret = "";
		
	@Autowired
	public TwitterListener(FeedItemDAO feedItemDAO) {
		this.feedItemDAO = feedItemDAO;
		connect();
	}
	
	public void connect() {		
		final StatusListener listener = new StatusListener() {
			
			public void onStatus(Status status) {
				final FeedItem tweetFeedItem = tweetToFeedItem(status);
				log.info("Received: " + tweetFeedItem);
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
				log.info(status.getMediaEntities().length);
				if (status.getMediaEntities().length > 0) {
					final MediaEntity mediaEntity = status.getMediaEntities()[0];
					log.info(mediaEntity);
					String type = mediaEntity.getType();
					String url = mediaEntity.getMediaURL();
					if (!Strings.isNullOrEmpty(type) && type.equals("photo") && !Strings.isNullOrEmpty(url)) {
						log.info("Using image url: " + url);
						return url;
					}
				}
				return null;
			}			
		};

		TwitterStream twitterStream = new TwitterApiFactory().getStreamingApi(consumerKey, consumerSecret, accessToken, accessSecret);
		twitterStream.addListener(listener);

		String[] london = { "twickenham" };
		twitterStream.filter(new FilterQuery().track(london));		    
	}
	
}
