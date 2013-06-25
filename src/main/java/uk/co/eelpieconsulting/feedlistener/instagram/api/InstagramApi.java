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

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpFetcher;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;

import com.google.common.collect.Lists;

public class InstagramApi {
	
	private static final String INSTAGRAM_API_V1_SUBSCRIPTIONS = "https://api.instagram.com/v1/subscriptions/";

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
		httpFetcher.post(post);
	}

	public void deleteAllsSubscriptions(String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {		
		final HttpFetcher httpFetcher = new HttpFetcher();
		HttpDelete delete = new HttpDelete(INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&object=all&client_id=" + clientId);
		httpFetcher.delete(delete);	
	}
	
}
