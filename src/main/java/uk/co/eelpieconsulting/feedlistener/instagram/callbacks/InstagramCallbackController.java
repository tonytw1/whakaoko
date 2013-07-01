package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
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
	
	private final String accessToken;
	private final String clientId;
	
	@Autowired
	public InstagramCallbackController(InstagramSubscriptionCallbackParser instagramSubscriptionCallbackParser, FeedItemDAO feedItemDAO,
			SubscriptionsDAO subscriptionsDAO,
			@Value("#{config['instagram.access.token']}") String accessToken,
			@Value("#{config['instagram.client.id']}") String clientId) {
		this.instagramSubscriptionCallbackParser = instagramSubscriptionCallbackParser;
		this.feedItemDAO = feedItemDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.accessToken = accessToken;
		this.clientId = clientId;
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
	public ModelAndView dataCallback(@RequestBody String body) throws IOException, JSONException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {		
		log.info("Received subscription callback post: " + body);
		
		List<Long> updatedSubscriptions = instagramSubscriptionCallbackParser.parse(body);
		log.info("Updated subscriptions in this callback: " + updatedSubscriptions);
		for (Long subscriptionId : updatedSubscriptions) {
			
			final InstagramSubscription subscription = subscriptionsDAO.getByInstagramId(subscriptionId);
			log.info(subscriptionId + ": " + subscription);
			
			if (subscription != null && subscription instanceof InstagramTagSubscription) {
				final String tag = ((InstagramTagSubscription) subscription).getTag();
				log.info("Fetching recent media for changed tag: " + tag);
				List<FeedItem> recentMedia = instagramApi.getRecentMediaForTag(tag, accessToken);				
				for (FeedItem feedItem : recentMedia) {
					feedItem.setSubscriptionId(subscription.getId());
				}				
				feedItemDAO.addAll(recentMedia);
			}
			
			if (subscription != null && subscription instanceof InstagramGeographySubscription) {
				log.info("Fetching recent media for changed geography: " + subscription.toString());								
				final long geoId = ((InstagramGeographySubscription) subscription).getGeoId();
				List<FeedItem> recentMedia = instagramApi.getRecentMediaForGeography(geoId, clientId);
				for (FeedItem feedItem : recentMedia) {
					feedItem.setSubscriptionId(subscription.getId());
				}
				feedItemDAO.addAll(recentMedia);
			}
		}
		return null;
	}
	
}
