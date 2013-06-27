package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import java.io.IOException;
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
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscripton;
import uk.co.eelpieconsulting.feedlistener.instagram.api.InstagramApi;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

@Controller
public class InstagramCallbackController {
	
	private static Logger log = Logger.getLogger(InstagramCallbackController.class);
	
	private static final String ACCESS_TOKEN = "";

	private final InstagramApi instagramApi;
	private final InstagramSubscriptionCallbackParser instagramSubscriptionCallbackParser;
	private final FeedItemDAO feedItemDAO;
	
	@Autowired
	public InstagramCallbackController(InstagramSubscriptionCallbackParser instagramSubscriptionCallbackParser, FeedItemDAO feedItemDAO) {
		this.instagramSubscriptionCallbackParser = instagramSubscriptionCallbackParser;
		this.feedItemDAO = feedItemDAO;
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
		
		List<InstagramSubscripton> subscriptions = instagramSubscriptionCallbackParser.parse(body);
		log.info("Updated subscriptions  in this callback: " + subscriptions);
		for (InstagramSubscripton subscription : subscriptions) {
			if (subscription.getObject().equals("tag")) {
				final String tag = subscription.getObjectId();
				log.info("Fetching recent media for changed tag: " + tag);
				List<FeedItem> recentMediaForTag = instagramApi.getRecentMediaForTag(tag, ACCESS_TOKEN);
				log.info("Adding recent media: " + recentMediaForTag);
				feedItemDAO.addAll(recentMediaForTag);
			}
		}
		
		return null;
	}
	
}
