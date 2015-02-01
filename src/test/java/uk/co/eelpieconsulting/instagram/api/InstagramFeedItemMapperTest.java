package uk.co.eelpieconsulting.instagram.api;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.common.collect.Lists;

public class InstagramFeedItemMapperTest {

	private final InstagramFeedItemMapper mapper = new InstagramFeedItemMapper();

	@Test
	public void canExtractLocationFromGeotaggedInstagramImage() throws Exception {	
		final List<FeedItem> feedItems = parseItems(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("instagramRecentMedia.json")));

		final LatLong latLong = feedItems.get(0).getLatLong();
		assertEquals(51.447123404, latLong.getLatitude(), 0);
		assertEquals(-0.323779282, latLong.getLongitude(), 0);
	}

	@Test
	public void shouldExtractUserInformationFromInstragramPosts() throws Exception {
		final List<FeedItem> feedItems = parseItems(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("instagramRecentMedia.json")));
		
		assertEquals("auser", feedItems.get(0).getAuthor());
	}

	private List<FeedItem> parseItems(final String json) throws JSONException {
		final List<FeedItem> feedItems = Lists.newArrayList();
		final JSONObject responseJson = new JSONObject(json);		
		final JSONArray data = responseJson.getJSONArray("data");
		for (int i = 0; i < data.length(); i++) {
			JSONObject imageJson = data.getJSONObject(i);			
			feedItems.add(mapper.createFeedItemFrom(imageJson));			
		}
		return feedItems;
	}
	
}
