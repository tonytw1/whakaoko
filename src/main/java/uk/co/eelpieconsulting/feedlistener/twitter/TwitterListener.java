package uk.co.eelpieconsulting.feedlistener.twitter;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

import com.google.common.collect.Sets;

@Component
public class TwitterListener {
		
	private final SubscriptionsDAO subscriptionsDAO;
	private final TwitterStatusListener twitterListener;
	
	private final String consumerKey;	
	private final String consumerSecret;
	private final String accessToken;
	private final String accessSecret;

	private TwitterStream twitterStream;
	
	@Autowired
	public TwitterListener(SubscriptionsDAO subscriptionsDAO, TwitterStatusListener twitterListener,
			@Value("#{config['twitter.consumer.key']}") String consumerKey,	
			@Value("#{config['twitter.consumer.secret']}") String consumerSecret,
			@Value("#{config['twitter.access.token']}") String accessToken,
			@Value("#{config['twitter.access.secret']}") String accessSecret) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.twitterListener = twitterListener;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessSecret = accessSecret;		
		connect();
	}
	
	public void connect() {		
		if (twitterStream != null) {
			twitterStream.cleanUp();
		}
		
		twitterStream = new TwitterApiFactory().getStreamingApi(consumerKey, consumerSecret, accessToken, accessSecret);
		twitterStream.addListener(twitterListener);
		
		final Set<String> tagsList = Sets.newHashSet();
		for (Subscription subscription : subscriptionsDAO.getSubscriptions()) {
			if (subscription.getId().startsWith("twitter")) {
				tagsList.add(((TwitterTagSubscription) subscription).getTag());
			}
		}
		
		if (!tagsList.isEmpty()) {
			final String[] tags = tagsList.toArray(new String[tagsList.size()]);
			twitterStream.filter(new FilterQuery().track(tags));
		}
	}
	
}
