package uk.co.eelpieconsulting.instagram.api;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpFetcher;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.InstagramGeographySubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramTagSubscription;

import com.google.common.collect.Lists;

public class InstagramApi {
	
	private static Logger log = Logger.getLogger(InstagramApi.class);
	
	private static final String INSTAGRAM_API_AUTHORIZE = "https://api.instagram.com/oauth/authorize/";
	private static final String INSTAGRAM_API_ACCESS_TOKEN = "https://api.instagram.com/oauth/access_token";
	private static final String INSTAGRAM_API_V1_SUBSCRIPTIONS = "https://api.instagram.com/v1/subscriptions";
	
	private static final String DATA = "data";
	
	private final InstagramFeedItemMapper mapper;
	private final HttpFetcher httpFetcher;
	
	public InstagramApi() {
		this.mapper = new InstagramFeedItemMapper();
		this.httpFetcher = new HttpFetcher();
	}

	public InstagramTagSubscription createTagSubscription(String tag, String clientId, String clientSecret, String callbackUrl, String channelId, String username) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnsupportedEncodingException, JSONException {		
		final List<NameValuePair> nameValuePairs = commonSubscriptionFields( clientId, clientSecret, callbackUrl);		
		nameValuePairs.add(new BasicNameValuePair("object", "tag"));
		nameValuePairs.add(new BasicNameValuePair("object_id", tag));
		
		final HttpPost post = new HttpPost(INSTAGRAM_API_V1_SUBSCRIPTIONS);
		HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
		post.setEntity(entity);
		
		try {
			final String response = httpFetcher.post(post);		
			final JSONObject responseJSON = new JSONObject(response);
			return new InstagramTagSubscription(
				responseJSON.getJSONObject(DATA).getString("object_id"),
				responseJSON.getJSONObject(DATA).getLong("id"), channelId, username);
		
		} catch (HttpBadRequestException e) {
			log.error("HTTP Bad request: " + e.getResponseBody());
			throw new RuntimeException(e);
		}
	}
	
	public InstagramGeographySubscription createGeographySubscription(LatLong latLong, int radius, String clientId, String clientSecret, String callbackUrl, String channelId, String username) throws UnsupportedEncodingException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException {		
		final List<NameValuePair> nameValuePairs = commonSubscriptionFields(clientId, clientSecret, callbackUrl);		
		nameValuePairs.add(new BasicNameValuePair("object", "geography"));
		nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(latLong.getLatitude())));
		nameValuePairs.add(new BasicNameValuePair("lng", Double.toString(latLong.getLongitude())));
		nameValuePairs.add(new BasicNameValuePair("radius", Integer.toString(radius)));
		
		final HttpPost post = new HttpPost(INSTAGRAM_API_V1_SUBSCRIPTIONS);
		HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
		post.setEntity(entity);
		
		try {
			final String response = httpFetcher.post(post);
			log.info(response);
		
			JSONObject responseJSON = new JSONObject(response);
		
			return new InstagramGeographySubscription(latLong, radius, responseJSON.getJSONObject("data").getLong("id"), responseJSON.getJSONObject("data").getLong("object_id"), channelId, username);
			
		} catch (HttpBadRequestException e) {
			log.error("HTTP Bad request: " + e.getResponseBody());
			throw new RuntimeException(e);
		}
	}
	
	public String getSubscriptions(String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {
		return httpFetcher.get(INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&client_id=" + clientId);
	}

	public void deleteAllSubscriptions(String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {		
		HttpDelete delete = new HttpDelete(INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&object=all&client_id=" + clientId);
		log.info("Delete all response: " + httpFetcher.delete(delete));
	}
	
	public void deleteSubscription(long id, String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {
		HttpDelete delete = new HttpDelete(INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&id=" + Long.toString(id) + "&client_id=" + clientId);
		log.info("Delete subscription response; " + httpFetcher.delete(delete));
	}
	
	public List<FeedItem> getRecentMediaForTag(String tag, String accessToken) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException {	
		final String response = httpFetcher.get("https://api.instagram.com/v1/tags/" + tag + "/media/recent" + "?access_token=" + accessToken);
		return parseFeedItems(response);
	}
	
	public List<FeedItem> getRecentMediaForGeography(long geoId, String clientId) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException {
		final String response = httpFetcher.get("https://api.instagram.com/v1/geographies/" + geoId + "/media/recent" + "?client_id=" + clientId);
		return parseFeedItems(response);			
	}
	
	public String getAuthorizeRedirectUrl(String clientId, String redirectUrl) {
		return INSTAGRAM_API_AUTHORIZE + "?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code";
	}
	
	public String getAccessToken(String clientId, String clientSecret,  String code, String redirectUrl) throws HttpNotFoundException, HttpForbiddenException, HttpFetchException, JSONException, UnsupportedEncodingException {
		final HttpPost post = new HttpPost(INSTAGRAM_API_ACCESS_TOKEN);
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
		nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
		nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
		nameValuePairs.add(new BasicNameValuePair("redirect_uri", redirectUrl));
		nameValuePairs.add(new BasicNameValuePair("code", code));
		HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
		post.setEntity(entity);
				
		try {
			final String response = httpFetcher.post(post);
	
			log.info("Got access token response: " + response);
			
			JSONObject responseJson = new JSONObject(response);
			return responseJson.getString("access_token");
		} catch (HttpBadRequestException e) {
			log.error(e.getResponseBody());
			throw new RuntimeException(e);
		}
	}
	
	private List<FeedItem> parseFeedItems(String recentMediaForTag) throws JSONException {
		final List<FeedItem> feedItems = Lists.newArrayList();
		final JSONObject responseJson = new JSONObject(recentMediaForTag);		
		
		final JSONArray data = responseJson.getJSONArray(DATA);
		log.info("Recent media response contains items: " + data.length());
		for (int i = 0; i < data.length(); i++) {
			JSONObject imageJson = data.getJSONObject(i);			
			feedItems.add(mapper.createFeedItemFrom(imageJson));
		}
		return feedItems;
	}
	
	private List<NameValuePair> commonSubscriptionFields(String clientId, String clientSecret, String callbackUrl) {
		log.info("Callback url: " + callbackUrl);
		final String verifyToken =  UUID.randomUUID().toString();
		
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
		nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
		nameValuePairs.add(new BasicNameValuePair("aspect", "media"));
		nameValuePairs.add(new BasicNameValuePair("callback_url", callbackUrl));
		nameValuePairs.add(new BasicNameValuePair("verify_token", verifyToken));
		return nameValuePairs;
	}
	
}
