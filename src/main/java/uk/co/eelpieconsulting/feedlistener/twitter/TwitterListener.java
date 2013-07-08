package uk.co.eelpieconsulting.feedlistener.twitter;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

import com.google.common.collect.Sets;

@Component
public class TwitterListener {
		
	private final SubscriptionsDAO subscriptionsDAO;
	private final TwitterStatusListener twitterListener;
	private final CredentialService credentialService;
	private TwitterStream twitterStream;
	
	@Autowired
	public TwitterListener(SubscriptionsDAO subscriptionsDAO, TwitterStatusListener twitterListener, CredentialService credentialService) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.twitterListener = twitterListener;
		this.credentialService = credentialService;	
		connect();
	}
	
	public void connect() {		
		if (twitterStream != null) {
			twitterStream.cleanUp();
		}
		
		twitterStream = new TwitterApiFactory().getStreamingApi(credentialService.getTwitterConsumerKey(), 
				credentialService.getTwitterConsumerSecret(), 
				credentialService.getTwitterAccessToken(), 
				credentialService.getTwitterAccessSecret());
		twitterStream.addListener(twitterListener);
		
		final Set<String> tagsList = Sets.newHashSet();
		for (Subscription subscription : subscriptionsDAO.getTwitterSubscriptions()) {
			tagsList.add(((TwitterTagSubscription) subscription).getTag());			
		}
		
		if (!tagsList.isEmpty()) {
			final String[] tags = tagsList.toArray(new String[tagsList.size()]);
			twitterStream.filter(new FilterQuery().track(tags));
		}
	}
	
}
