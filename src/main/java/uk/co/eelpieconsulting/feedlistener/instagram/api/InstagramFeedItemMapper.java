package uk.co.eelpieconsulting.feedlistener.instagram.api;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

public class InstagramFeedItemMapper {
	
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
		
		LatLong latLong = null;
		if (imageJson.has(LOCATION) && !imageJson.isNull(LOCATION)) {
			final JSONObject locationJson = imageJson.getJSONObject(LOCATION);
			latLong = new LatLong(locationJson.getDouble("latitude"), locationJson.getDouble("longitude"));	// TODO preserve name and id if available.
		}
		return new FeedItem(caption, url, null, createdTime.toDate(), latLong, imageUrl);
	}


}
