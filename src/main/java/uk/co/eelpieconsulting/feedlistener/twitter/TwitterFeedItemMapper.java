package uk.co.eelpieconsulting.feedlistener.twitter;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Status;
import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

@Component
public class TwitterFeedItemMapper {

	public FeedItem createFeedItemFrom(Status status) {
		final Place place = extractLocationFrom(status);				
		final String mediaUrl = extractImageUrl(status);
		return new FeedItem(extractHeadingFrom(status), extractUrlFrom(status), null, status.getCreatedAt(), place, mediaUrl, null);
	}
	
	private String extractHeadingFrom(Status status) {
		return "@" + status.getUser().getScreenName() + ": " + status.getText();
	}

	private String extractUrlFrom(Status status) {
		return "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + Long.toString(status.getId());
	}

	private Place extractLocationFrom(Status status) {
		Place place = null;
		if (status.getGeoLocation() != null) {
			final GeoLocation geoLocation = status.getGeoLocation();
			LatLong latLong = new LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
			place = new Place(null, latLong, null);	// TODO capture twitter location ids
		}
		return place;
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
	
}
