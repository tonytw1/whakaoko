package uk.co.eelpieconsulting.feedlistener.twitter;

import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

@Component
public class TwitterApiFactory {
	
	public Twitter getOauthedTwitterApi(String consumerKey, String consumerSecret, String accessToken, String accessSecret) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
		return new TwitterFactory(configBuilder.build()).getInstance(new AccessToken(accessToken, accessSecret));
	}

	public TwitterStream getStreamingApi(String consumerKey, String consumerSecret, String accessToken, String accessSecret) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
		return new TwitterStreamFactory(configBuilder.build()).getInstance(new AccessToken(accessToken, accessSecret));
	}
	
}
