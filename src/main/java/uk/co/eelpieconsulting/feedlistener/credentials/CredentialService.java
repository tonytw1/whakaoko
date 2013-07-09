package uk.co.eelpieconsulting.feedlistener.credentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

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
	public CredentialService(@Value("#{config['instagram.client.id']}") String instagramClientId, 
			@Value("#{config['instagram.client.secret']}") String instagramClientSecret,
			@Value("#{config['twitter.consumer.key']}") String twitterConsumerKey, 
			@Value("#{config['twitter.consumer.secret']}") String twitterConsumerSecret) {
		super();
		this.instagramClientId = instagramClientId;
		this.instagramClientSecret = instagramClientSecret;
		this.twitterConsumerKey = twitterConsumerKey;
		this.twitterConsumerSecret = twitterConsumerSecret;
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
	public void setTwitterAccessToken(String twitterAccessToken) {
		this.twitterAccessToken = twitterAccessToken;
	}
	public String getTwitterAccessSecret() {
		return twitterAccessSecret;
	}
	public void setTwitterAccessSecret(String twitterAccessSecret) {
		this.twitterAccessSecret = twitterAccessSecret;
	}

	public String getInstagramAccessToken() {
		return instagramAccessToken;
	}
	public void setInstagramAccessToken(String accessToken) {
		this.instagramAccessToken = accessToken;		
	}
	
	public boolean hasTwitterAccessToken() {
		return !Strings.isNullOrEmpty(twitterAccessToken) && !Strings.isNullOrEmpty(twitterAccessSecret);
	}
	
	public boolean hasInstagramAccessToken() {
		return !Strings.isNullOrEmpty(instagramAccessToken);
	}
	
}
