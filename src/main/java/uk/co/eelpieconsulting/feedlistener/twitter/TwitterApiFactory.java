package uk.co.eelpieconsulting.feedlistener.twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;

@Component
public class TwitterApiFactory {
	
	private CredentialService credentialService;
	
	@Autowired
	public TwitterApiFactory(CredentialService credentialService) {
		this.credentialService = credentialService;
	}

	public Twitter getOauthedTwitterApi(String accessToken, String accessSecret) {
		return new TwitterFactory(buildConfig()).getInstance(new AccessToken(accessToken, accessSecret));
	}
	
	public TwitterStream getStreamingApi(String accessToken, String accessSecret) {
		return new TwitterStreamFactory(buildConfig()).getInstance(new AccessToken(accessToken, accessSecret));
	}

	public Twitter getTwitterApi() {
		return new TwitterFactory(buildConfig()).getInstance();
	}
	
	private Configuration buildConfig() {
		final ConfigurationBuilder configBuilder = new ConfigurationBuilder().
				setOAuthConsumerKey(credentialService.getTwitterConsumerKey()).
				setOAuthConsumerSecret(credentialService.getTwitterConsumerSecret());
		return configBuilder.build();
	}
	
}
