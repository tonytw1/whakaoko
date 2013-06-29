package uk.co.eelpieconsulting.feedlistener.instagram.api;

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

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpFetcher;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.common.collect.Lists;

public class InstagramApi {
	
	private static Logger log = Logger.getLogger(InstagramApi.class);
	
	private static final String INSTAGRAM_API_AUTHORIZE = "https://api.instagram.com/oauth/authorize/";
	private static final String INSTAGRAM_API_ACCESS_TOKEN = "https://api.instagram.com/oauth/access_token";
	private static final String INSTAGRAM_API_V1_SUBSCRIPTIONS = "https://api.instagram.com/v1/subscriptions/";
	
	private static final String DATA = "data";
	
	private final InstagramFeedItemMapper mapper;
	
	public InstagramApi() {
		this.mapper = new InstagramFeedItemMapper();
	}

	public void createTagSubscription(String tag, String clientId, String clientSecret, String callbackUrl) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnsupportedEncodingException {
		final String verifyToken =  UUID.randomUUID().toString();
		
		final HttpPost post = new HttpPost(INSTAGRAM_API_V1_SUBSCRIPTIONS);
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
		nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
		nameValuePairs.add(new BasicNameValuePair("aspect", "media"));
		nameValuePairs.add(new BasicNameValuePair("object", "tag"));
		nameValuePairs.add(new BasicNameValuePair("object_id", tag));
		nameValuePairs.add(new BasicNameValuePair("callback_url", callbackUrl));
		nameValuePairs.add(new BasicNameValuePair("verify_token", verifyToken));
		HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
		post.setEntity(entity);

		final HttpFetcher httpFetcher = new HttpFetcher();
		final String response = httpFetcher.post(post);
		log.info(response);
	}
	
	public String getSubscriptions(String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {
		HttpFetcher httpFetcher = new HttpFetcher();
		return httpFetcher.get("https://api.instagram.com/v1/subscriptions" + "?client_secret=" + clientSecret + "&client_id=" + clientId);
	}

	public void deleteAllSubscriptions(String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {		
		final HttpFetcher httpFetcher = new HttpFetcher();
		HttpDelete delete = new HttpDelete(INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&object=all&client_id=" + clientId);
		httpFetcher.delete(delete);	
	}
	
	public void deleteSubscription(String tag) {
		// TODO Auto-generated method stub	
	}
	
	public List<FeedItem> getRecentMediaForTag(String tag, String accessToken) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException {	
		final HttpFetcher httpFetcher = new HttpFetcher();		
		final String response = httpFetcher.get("https://api.instagram.com/v1/tags/" + tag + "/media/recent" + "?access_token=" + accessToken);
		System.out.println(response);
		return parseFeedItems(response);
	}
	
	public String getAuthorizeRedirectUrl(String clientId, String redirectUrl) {
		return INSTAGRAM_API_AUTHORIZE + "?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code";
	}
	
	public String getAccessToken(String clientId, String clientSecret,  String code, String redirectUrl) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException, UnsupportedEncodingException {
		final HttpPost post = new HttpPost(INSTAGRAM_API_ACCESS_TOKEN);
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
		nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
		nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
		nameValuePairs.add(new BasicNameValuePair("redirect_uri", redirectUrl));
		nameValuePairs.add(new BasicNameValuePair("code", code));
		HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
		post.setEntity(entity);
		
		final HttpFetcher httpFetcher = new HttpFetcher();
		final String response = httpFetcher.post(post);
		log.info("Got access token response: " + response);
		
		JSONObject responseJson = new JSONObject(response);
		return responseJson.getString("access_token");	
	}

	private List<FeedItem> parseFeedItems(String recentMediaForTag) throws JSONException {
		final List<FeedItem> feedItems = Lists.newArrayList();
		final JSONObject responseJson = new JSONObject(recentMediaForTag);		
		
		final JSONArray data = responseJson.getJSONArray(DATA);
		for (int i = 0; i < data.length(); i++) {
			JSONObject imageJson = data.getJSONObject(i);			
			feedItems.add(mapper.createFeedItemFrom(imageJson));
		}
		return feedItems;
	}
	
}
