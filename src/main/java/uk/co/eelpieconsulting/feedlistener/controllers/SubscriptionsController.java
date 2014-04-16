package uk.co.eelpieconsulting.feedlistener.controllers;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.CredentialsRequiredException;
import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.annotations.Timed;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscriptionManager;
import uk.co.eelpieconsulting.feedlistener.model.InstagramGeographySubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller;
import uk.co.eelpieconsulting.feedlistener.rss.RssSubscriptionManager;
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener;
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterSubscriptionManager;

import com.google.common.base.Strings;
import com.mongodb.MongoException;

@Controller
public class SubscriptionsController {
	
	private final static Logger log = Logger.getLogger(SubscriptionsController.class);
	
	private UsersDAO usersDAO;
	private SubscriptionsDAO subscriptionsDAO;
	private RssPoller rssPoller;
	private TwitterListener twitterListener;
	private InstagramSubscriptionManager instagramSubscriptionManager;
	private UrlBuilder urlBuilder;
	private FeedItemDAO feedItemDAO;
	private TwitterSubscriptionManager twitterSubscriptionManager;
	private RssSubscriptionManager rssSubscriptionManager;
	private ViewFactory viewFactory;
	private FeedItemPopulator feedItemPopulator;
	
	public SubscriptionsController() {
	}
	
	@Autowired
	public SubscriptionsController(UsersDAO usersDAO, SubscriptionsDAO subscriptionsDAO, RssPoller rssPoller, TwitterListener twitterListener, 
			InstagramSubscriptionManager instagramSubscriptionManager, UrlBuilder urlBuilder,
			FeedItemDAO feedItemDAO,
			TwitterSubscriptionManager twitterSubscriptionManager,
			RssSubscriptionManager rssSubscriptionManager,
			ViewFactory viewFactory, FeedItemPopulator feedItemPopulator) {
		this.usersDAO = usersDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.rssPoller = rssPoller;
		this.twitterListener = twitterListener;
		this.instagramSubscriptionManager = instagramSubscriptionManager;
		this.urlBuilder = urlBuilder;
		this.feedItemDAO = feedItemDAO;
		this.twitterSubscriptionManager = twitterSubscriptionManager;
		this.rssSubscriptionManager = rssSubscriptionManager;
		this.viewFactory = viewFactory;
		this.feedItemPopulator = feedItemPopulator;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/{id}/items", method=RequestMethod.GET)
	public ModelAndView subscriptionItems(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String format) throws UnknownHostException, MongoException, UnknownSubscriptionException, UnknownUserException {
		usersDAO.getByUsername(username);
		
		Subscription subscription = subscriptionsDAO.getById(username, id);
		
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		if (!Strings.isNullOrEmpty(format) && format.equals("rss")) {
			mv = new ModelAndView(viewFactory.getRssView(subscription.getName(), urlBuilder.getSubscriptionUrl(subscription), ""));
		}
		
		feedItemPopulator.populateFeedItems(subscription, page, mv, "data");		
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/subscriptions/{id}", method=RequestMethod.GET)	
	public ModelAndView subscriptionJson(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException, UnknownSubscriptionException, UnknownUserException {
		usersDAO.getByUsername(username);
		
		Subscription subscription = subscriptionsDAO.getById(username, id);
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		feedItemPopulator.populateFeedItems(subscription, page, mv, "data");		
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/{id}/delete")	// TODO should be a HTTP DELETE
	public ModelAndView deleteSubscription(@PathVariable String username, @PathVariable String id) throws UnknownHostException, MongoException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnknownSubscriptionException, UnknownUserException {
		usersDAO.getByUsername(username);
		
		Subscription subscription = subscriptionsDAO.getById(username, id);
		if (subscription == null) {
			// TODO 404
			return null;
		}
		
		feedItemDAO.deleteSubscriptionFeedItems(subscription);
		subscriptionsDAO.delete(subscription);
						
		if (subscription.getId().startsWith("twitter")) {
			twitterListener.connect();
		}
		if (subscription.getId().startsWith("instagram")) {
			instagramSubscriptionManager.requestUnsubscribeFrom(((InstagramSubscription) subscription).getSubscriptionId());
		}
		
		return new ModelAndView(viewFactory.getJsonView()).addObject("data", "ok");
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions", method=RequestMethod.GET)
	public ModelAndView subscriptions(@PathVariable String username) throws UnknownUserException {
		usersDAO.getByUsername(username);
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsDAO.getSubscriptions());
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/feeds", method=RequestMethod.POST)
	public ModelAndView addFeedSubscription(@PathVariable String username, @RequestParam String url, @RequestParam String channel) throws UnknownUserException {
		usersDAO.getByUsername(username);
		
		Subscription subscription = rssSubscriptionManager.requestFeedSubscription(url, channel, username);
		subscriptionsDAO.add(subscription);
		log.info("Added subscription: " + subscription);
		
		rssPoller.run(subscription);
		
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscription);
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/twitter/tags", method=RequestMethod.POST)
	public ModelAndView addTwitterTagSubscription(@PathVariable String username, @RequestParam String tag, @RequestParam String channel) throws CredentialsRequiredException, UnknownUserException {
		log.info("Twitter tag: " + tag);
		twitterSubscriptionManager.requestTagSubscription(tag, channel, username);
		
		return new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/instagram/tags", method=RequestMethod.POST)
	public ModelAndView addInstagramTagSubscription(@PathVariable String username, 
			@RequestParam String tag, @RequestParam String channel) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, UnsupportedEncodingException, HttpFetchException, JSONException, UnknownUserException {		
		log.info("Instagram tag");
		InstagramSubscription subscription = instagramSubscriptionManager.requestInstagramTagSubscription(tag, channel, username);
		subscriptionsDAO.add(subscription);
		
		return new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/instagram/geography", method=RequestMethod.POST)
	public ModelAndView addInstagramTagSubscription(@PathVariable String username, 
			@RequestParam double latitude,
			@RequestParam double longitude, 
			@RequestParam int radius,
			@RequestParam String channel) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, UnsupportedEncodingException, HttpFetchException, JSONException, UnknownUserException {
		LatLong latLong = new LatLong(latitude, longitude);
		
		InstagramGeographySubscription instagramGeographySubscription = instagramSubscriptionManager.requestInstagramGeographySubscription(latLong, radius, channel, username);
		log.info("Saving subscription: " + instagramGeographySubscription);
		subscriptionsDAO.add(instagramGeographySubscription);
				
		return new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
	}
	
    @ExceptionHandler(UnknownSubscriptionException.class)	// TODO make global
    @ResponseStatus(value=org.springframework.http.HttpStatus.NOT_FOUND)
    public ModelAndView unknownSubscriptionException(UnknownSubscriptionException e) {
            return new ModelAndView(viewFactory.getJsonView()).addObject("data", "Not found");
    }

}
