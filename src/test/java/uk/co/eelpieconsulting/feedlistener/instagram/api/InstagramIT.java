package uk.co.eelpieconsulting.feedlistener.instagram.api;

import java.util.List;

import org.junit.Test;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

public class InstagramIT {
	
	private final String CLIENT_ID = "";
	private final String CLIENT_SECRET = "";	
	private static final String ACCESS_TOKEN = "";

	private final InstagramApi api;

	public InstagramIT() {
		this.api = new InstagramApi();
	}
	
	@Test
	public void canFetchRecentPostForTag() throws Exception {
		List<FeedItem> recentMediaForTag = api.getRecentMediaForTag("twickenham", ACCESS_TOKEN);		
		System.out.println(recentMediaForTag);
	}

	@Test
	public void canSubscribe() throws Exception {
		try {
			api.createTagSubscription("twickenham", CLIENT_ID, CLIENT_SECRET, "http://localhost/instagram/callback", "a-channel";
			
		} catch (HttpBadRequestException e) {
			System.out.println(e.getResponseBody());
		}
	}
	
	@Test
	public void canListAllSubscriptions() throws Exception {
		System.out.println(api.getSubscriptions(CLIENT_ID, CLIENT_SECRET));
	}
	
	@Test
	public void canUnsubscribeAll() throws Exception {
		InstagramApi api = new InstagramApi();
		try {
			api.deleteAllSubscriptions(CLIENT_ID, CLIENT_SECRET);
		} catch (HttpBadRequestException e) {
			System.out.println(e.getResponseBody());
		}
	}

}
