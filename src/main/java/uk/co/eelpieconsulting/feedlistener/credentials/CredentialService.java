package uk.co.eelpieconsulting.feedlistener.credentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CredentialService {

	private String instagramAccessToken;
	private String instagramClientId;
	private String instagramClientSecret;

	private String twitterConsumerKey;
	private String twitterConsumerSecret;
	private String twitterAccessToken;
	private String twitterAccessSecret;
	
	@Autowired
	public CredentialService(@Value("#{config['instagram.access.token']}") String instagramAccessToken,
			@Value("#{config['instagram.client.id']}") String instagramClientId, 
			@Value("#{config['instagram.client.secret']}") String instagramClientSecret,
			@Value("#{config['twitter.consumer.key']}") String twitterConsumerKey, 
			@Value("#{config['twitter.consumer.secret']}") String twitterConsumerSecret,
			@Value("#{config['twitter.access.token']}") String twitterAccessToken, 
			@Value("#{config['twitter.access.secret']}") String twitterAccessSecret) {
		super();
		this.instagramAccessToken = instagramAccessToken;
		this.instagramClientId = instagramClientId;
		this.instagramClientSecret = instagramClientSecret;
		this.twitterConsumerKey = twitterConsumerKey;
		this.twitterConsumerSecret = twitterConsumerSecret;
		this.twitterAccessToken = twitterAccessToken;
		this.twitterAccessSecret = twitterAccessSecret;
	}
	public String getInstagramAccessToken() {
		return instagramAccessToken;
	}
	public String getInstagramClientId() {
		return instagramClientId;
	}
	public String getInstagramClientSecret() {
		return instagramClientSecret;
	}
	public String getTwitterConsumerKey() {
		return twitterConsumerKey;
	}
	public String getTwitterConsumerSecret() {
		return twitterConsumerSecret;
	}
	public String getTwitterAccessToken() {
		return twitterAccessToken;
	}
	public String getTwitterAccessSecret() {
		return twitterAccessSecret;
	}
	
}
