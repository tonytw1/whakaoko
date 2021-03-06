package uk.co.eelpieconsulting.feedlistener.twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.CredentialsRequiredException;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

@Component
public class TwitterSubscriptionManager {
	
	private final CredentialService credentialService;
	private final SubscriptionsDAO subscriptionsDAO;
	private final TwitterListener twitterListener;
	
	@Autowired
	public TwitterSubscriptionManager(CredentialService credentialService, SubscriptionsDAO subscriptionsDAO, TwitterListener twitterListener) {
		this.credentialService = credentialService;
		this.subscriptionsDAO = subscriptionsDAO;
		this.twitterListener = twitterListener;
	}
	
	public void requestTagSubscription(String tag, String channel, String username) throws CredentialsRequiredException, UnknownUserException {
		if (!credentialService.hasTwitterAccessToken(username)) {
			throw new CredentialsRequiredException();
		}
		
		subscriptionsDAO.add(new TwitterTagSubscription(tag, channel, username));
		reconnect();		
	}

	public void reconnect() throws CredentialsRequiredException, UnknownUserException {
		twitterListener.connect();		
	}

}
