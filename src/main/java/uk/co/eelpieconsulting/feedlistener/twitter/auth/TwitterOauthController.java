package uk.co.eelpieconsulting.feedlistener.twitter.auth;

import java.net.UnknownHostException;
import java.util.Map;

;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterApiFactory;
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterSubscriptionManager;

import com.google.common.collect.Maps;

@Controller
public class TwitterOauthController {
	
	private static Logger log = LogManager.getLogger(TwitterOauthController.class);
		
	private final CredentialService credentialService;
	private final TwitterApiFactory twitterApiFactory;
	private final TwitterSubscriptionManager twitterSubscriptionManager;
	private final UrlBuilder urlBuilder;
	
	private final Map<String, RequestToken> requestTokens;
	
	@Autowired
	public TwitterOauthController(CredentialService credentialService, TwitterApiFactory twitterApiFactory, TwitterSubscriptionManager twitterSubscriptionManager,
			UrlBuilder urlBuilder) {
		this.credentialService = credentialService;
		this.twitterApiFactory = twitterApiFactory;
		this.twitterSubscriptionManager = twitterSubscriptionManager;
		this.urlBuilder = urlBuilder;
		requestTokens = Maps.newConcurrentMap();
	}
	
	@RequestMapping(value="/twitter/authorise/{username}", method=RequestMethod.GET)
	public ModelAndView authorize(@PathVariable String username) throws TwitterException {
		final Twitter twitterApi = twitterApiFactory.getTwitterApi();
		
		RequestToken requestToken = twitterApi.getOAuthRequestToken(urlBuilder.getTwitterCallback(username));
		log.info("Stashing request token: " + requestToken.getToken());
		requestTokens.put(requestToken.getToken(), requestToken);
		
		final String authorizeRedirectUrl = requestToken.getAuthorizationURL();

		log.info("Redirecting user to twitter: " + authorizeRedirectUrl);
		return new ModelAndView(new RedirectView(authorizeRedirectUrl));
	}
	
	@RequestMapping(value="/twitter/callback/{username}", method=RequestMethod.GET)
	public ModelAndView callback(@PathVariable String username, @RequestParam(value="oauth_token", required=true) String token,
			@RequestParam(value="oauth_verifier", required=true) String verifier) throws TwitterException, UnknownHostException, UnknownUserException {		
		log.info("Received Twitter oauth callback: oauth_token: " + token + ", oauth_verifier: " + verifier);

		log.info("Popping stashed request token: " + token);
		final RequestToken requestToken = requestTokens.get(token);
		if (requestToken == null) {
			log.warn("Twitter callback mentions an unknown request token: " + token);
			throw new RuntimeException();
		}
		
		final Twitter twitterApi = twitterApiFactory.getTwitterApi();
		final AccessToken accessToken = twitterApi.getOAuthAccessToken(requestToken, verifier);
		
		log.info("Got twitter access token: " + accessToken);
		log.info("Got twitter access token: " + accessToken.getToken());
		log.info("Got twitter access secret: " + accessToken.getTokenSecret());

		credentialService.setTwitterAccessTokenForUser(username, accessToken.getToken());
		credentialService.setTwitterAccessSecretForUser(username, accessToken.getTokenSecret());

		twitterSubscriptionManager.reconnect();		
		return null;
	}
	
}
