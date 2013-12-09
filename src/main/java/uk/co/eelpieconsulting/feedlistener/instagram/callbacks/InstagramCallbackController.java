package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.instagram.api.InstagramApi;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.InstagramGeographySubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramTagSubscription;

@Controller
public class InstagramCallbackController {
	
	private static Logger log = Logger.getLogger(InstagramCallbackController.class);
	
	private final InstagramApi instagramApi;
	private final InstagramSubscriptionCallbackParser instagramSubscriptionCallbackParser;
	private final SubscriptionsDAO subscriptionsDAO;
	private final FeedItemDAO feedItemDAO;
	private final CredentialService credentialService;
	
	@Autowired
	public InstagramCallbackController(InstagramSubscriptionCallbackParser instagramSubscriptionCallbackParser, FeedItemDAO feedItemDAO,
			SubscriptionsDAO subscriptionsDAO, CredentialService credentialService) {
		this.instagramSubscriptionCallbackParser = instagramSubscriptionCallbackParser;
		this.feedItemDAO = feedItemDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.credentialService = credentialService;
		this.instagramApi = new InstagramApi();		
	}

	@RequestMapping(value="/instagram/callback", method=RequestMethod.GET)
	public ModelAndView subscribeCallback(@RequestParam(value="hub.mode", required=false) String hubMode,
			@RequestParam(value="hub.challenge", required=false) String hubChallenge,
			@RequestParam(value="hub.verify_token", required=false) String hubToken,
			HttpServletResponse response) throws IOException {
		
		log.info("Received callback: " + hubMode + ", " + hubChallenge + ", " + hubToken);
		response.getOutputStream().print(hubChallenge);
		response.flushBuffer();
		return null;
	}
	
	@RequestMapping(value="/instagram/callback", method=RequestMethod.POST)
	public ModelAndView dataCallback(@RequestBody String body) throws IOException, JSONException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnknownUserException {
		final String username = "tonytw1";	// TODO user specific call back urls
				
		log.info("Received subscription callback post: " + body);
		
		final List<Long> updatedInstagramSubscriptions = instagramSubscriptionCallbackParser.parse(body);
		log.info("Updated instagram subscription ids in this callback: " + updatedInstagramSubscriptions);
		for (Long instagramSubscriptionId : updatedInstagramSubscriptions) {
			
			final InstagramSubscription subscription = subscriptionsDAO.getByInstagramId(instagramSubscriptionId);
			
			if (subscription != null && subscription instanceof InstagramTagSubscription) {
				final String tag = ((InstagramTagSubscription) subscription).getTag();
				
				log.info("Fetching recent media for changed tag: " + tag);
				List<FeedItem> recentMedia = instagramApi.getRecentMediaForTag(tag, credentialService.getInstagramAccessTokenForUser(username));								
				Date latestItemDate = populateFeedItemSubscriptionIdAndExtractLatestItemDate(subscription, recentMedia);				
				feedItemDAO.addAll(recentMedia);
				
				subscription.setLatestItemDate(latestItemDate);						
				subscriptionsDAO.save(subscription);
			}
			
			if (subscription != null && subscription instanceof InstagramGeographySubscription) {
				final long geoId = ((InstagramGeographySubscription) subscription).getGeoId();

				log.info("Fetching recent media for changed geography: " + subscription.toString());								
				List<FeedItem> recentMedia = instagramApi.getRecentMediaForGeography(geoId, credentialService.getInstagramClientId());				
				Date latestItemDate = populateFeedItemSubscriptionIdAndExtractLatestItemDate(subscription, recentMedia);				
				feedItemDAO.addAll(recentMedia);
				
				subscription.setLatestItemDate(latestItemDate);						
				subscriptionsDAO.save(subscription);
			}
			
		}
		return null;
	}

	private Date populateFeedItemSubscriptionIdAndExtractLatestItemDate(final InstagramSubscription subscription, List<FeedItem> recentMedia) {
		Date latestItemDate = null;
		for (FeedItem feedItem : recentMedia) {
			feedItem.setSubscriptionId(subscription.getId());
			
			final Date feedItemDate = feedItem.getDate();
			if (feedItemDate != null && (latestItemDate == null || feedItemDate.after(latestItemDate))) {
				latestItemDate =  feedItemDate;
			}
			
		}
		return latestItemDate;
	}
	
}
