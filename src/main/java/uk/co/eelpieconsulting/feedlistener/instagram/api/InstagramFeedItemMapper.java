package uk.co.eelpieconsulting.feedlistener.instagram.api;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

public class InstagramFeedItemMapper {
	
	private static final String LONGITUDE = "longitude";

	private static final String LATITUDE = "latitude";

	private static Logger log = Logger.getLogger(InstagramFeedItemMapper.class);
	
	private static final String LOCATION = "location";
	private static final String LINK = "link";
	private static final String CREATED_TIME = "created_time";
	private static final String TEXT = "text";
	private static final String URL = "url";
	private static final String STANDARD_RESOLUTION = "standard_resolution";
	private static final String CAPTION = "caption";
	private static final String IMAGES = "images";
		
	public FeedItem createFeedItemFrom(JSONObject imageJson) throws JSONException {
		String imageUrl = null;
		if (imageJson.has(IMAGES)) {
			JSONObject imagesJson = imageJson.getJSONObject(IMAGES);
			imageUrl = imagesJson.getJSONObject(STANDARD_RESOLUTION).getString(URL);
		}

		String caption = null;
		if (imageJson.has(CAPTION) && !imageJson.isNull(CAPTION)) {
			JSONObject captionJson = imageJson.getJSONObject(CAPTION);
			caption = captionJson.getString(TEXT);
		}
		
		DateTime createdTime = new DateTime(imageJson.getLong(CREATED_TIME) * 1000);

		final String url = imageJson.getString(LINK);

		Place place = null;
		if (imageJson.has(LOCATION) && !imageJson.isNull(LOCATION)) {
			final JSONObject locationJson = imageJson.getJSONObject(LOCATION);
			log.info(locationJson.toString());
			if (locationJson.has(LATITUDE) && locationJson.has(LONGITUDE)) {
				LatLong latLong = new LatLong(locationJson.getDouble(LATITUDE), locationJson.getDouble(LONGITUDE));	// TODO preserve name and id if available.
				place = new Place(null, latLong, null);
			} else {
				log.warn("Location has no lat long: " + locationJson.toString());
			}
		}
		
		return new FeedItem(caption, url, null, createdTime.toDate(), place, imageUrl);
	}


}
