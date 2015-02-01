package uk.co.eelpieconsulting.instagram.api;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

public class InstagramFeedItemMapper {

	private final static Logger log = Logger.getLogger(InstagramFeedItemMapper.class);

	private static final String USERNAME = "username";
	private static final String USER = "user";	
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";	
	private static final String LOCATION = "location";
	private static final String LINK = "link";
	private static final String CREATED_TIME = "created_time";
	private static final String TEXT = "text";
	private static final String URL = "url";
	private static final String STANDARD_RESOLUTION = "standard_resolution";
	private static final String CAPTION = "caption";
	private static final String IMAGES = "images";
	
	public FeedItem createFeedItemFrom(JSONObject json) throws JSONException {
		String imageUrl = null;
		if (json.has(IMAGES)) {
			JSONObject imagesJson = json.getJSONObject(IMAGES);
			imageUrl = imagesJson.getJSONObject(STANDARD_RESOLUTION).getString(URL);
		}

		String caption = null;
		if (json.has(CAPTION) && !json.isNull(CAPTION)) {
			JSONObject captionJson = json.getJSONObject(CAPTION);
			caption = captionJson.getString(TEXT);
		}
		
		DateTime createdTime = new DateTime(json.getLong(CREATED_TIME) * 1000);

		final String url = json.getString(LINK);

		Place place = null;
		if (json.has(LOCATION) && !json.isNull(LOCATION)) {
			final JSONObject locationJson = json.getJSONObject(LOCATION);
			if (locationJson.has(LATITUDE) && locationJson.has(LONGITUDE)) {
				LatLong latLong = new LatLong(locationJson.getDouble(LATITUDE), locationJson.getDouble(LONGITUDE));	// TODO preserve name and id if available.
				place = new Place(null, latLong, null);
			} else {
				log.warn("Location has no lat long: " + locationJson.toString());
			}
		}
		
		String author = null;
		if (json.has(USER)) {
			final JSONObject userJson = json.getJSONObject(USER);
			if (userJson.has(USERNAME)) {
				author = userJson.getString(USERNAME);
			}
		}
		
		return new FeedItem(caption, url, null, createdTime.toDate(), place, imageUrl, author);
	}
	
}
