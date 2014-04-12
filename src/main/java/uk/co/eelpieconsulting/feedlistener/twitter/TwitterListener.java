package uk.co.eelpieconsulting.feedlistener.twitter;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import uk.co.eelpieconsulting.feedlistener.CredentialsRequiredException;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

import com.google.common.collect.Sets;

@Component
public class TwitterListener {
	
	private static Logger log = Logger.getLogger(TwitterListener.class);

	private static final String USER = "";	// TODO needs to be multitenant
	
	private final SubscriptionsDAO subscriptionsDAO;
	private final TwitterStatusListener twitterListener;
	private final CredentialService credentialService;
	private final TwitterApiFactory twitterApiFactory;
	
	private TwitterStream twitterStream;
	
	@Autowired
	public TwitterListener(SubscriptionsDAO subscriptionsDAO, TwitterStatusListener twitterListener, CredentialService credentialService, TwitterApiFactory twitterApiFactory) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.twitterListener = twitterListener;
		this.credentialService = credentialService;
		this.twitterApiFactory = twitterApiFactory;
		
		try {
			connect();			
		} catch (CredentialsRequiredException e) {
			log.warn("No twitter credentials available; not connecting on start up");
		}
	}
	
	public void connect() throws CredentialsRequiredException {
		if (twitterStream != null) {
			twitterStream.cleanUp();
		}
		
		if (!credentialService.hasTwitterAccessToken(USER)) {
			log.warn("No twitter credentials available for user '" + USER + "'; not connecting");
			throw new CredentialsRequiredException();
		}

		String twitterAccessTokenForUser = credentialService.getTwitterAccessTokenForUser(USER);
		String twitterAccessSecretForUser = credentialService.getTwitterAccessSecretForUser(USER);
		log.info("Using twitter access credentials: " + twitterAccessTokenForUser + ", " + twitterAccessSecretForUser);
		
		twitterStream = twitterApiFactory.getStreamingApi(
				twitterAccessTokenForUser, 
				twitterAccessSecretForUser);
		
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
