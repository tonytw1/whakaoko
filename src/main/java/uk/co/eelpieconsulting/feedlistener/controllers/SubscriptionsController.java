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
import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.annotations.Timed;
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;
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
	
	private static int MAX_FEED_ITEMS = 20;

	private static Logger log = Logger.getLogger(SubscriptionsController.class);
	
	private UsersDAO usersDAO;
	private SubscriptionsDAO subscriptionsDAO;
	private RssPoller rssPoller;
	private TwitterListener twitterListener;
	private InstagramSubscriptionManager instagramSubscriptionManager;
	private UrlBuilder urlBuilder;
	private FeedItemDAO feedItemDAO;
	private TwitterSubscriptionManager twitterSubscriptionManager;
	private ChannelsDAO channelsDAO;
	private RssSubscriptionManager rssSubscriptionManager;
	private ViewFactory viewFactory;
	
	public SubscriptionsController() {
	}
	
	@Autowired
	public SubscriptionsController(UsersDAO usersDAO, SubscriptionsDAO subscriptionsDAO, RssPoller rssPoller, TwitterListener twitterListener, 
			InstagramSubscriptionManager instagramSubscriptionManager, UrlBuilder urlBuilder,
			FeedItemDAO feedItemDAO,
			ChannelsDAO channelsDAO,
			TwitterSubscriptionManager twitterSubscriptionManager,
			RssSubscriptionManager rssSubscriptionManager,
			ViewFactory viewFactory) {
		this.usersDAO = usersDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.rssPoller = rssPoller;
		this.twitterListener = twitterListener;
		this.instagramSubscriptionManager = instagramSubscriptionManager;
		this.urlBuilder = urlBuilder;
		this.feedItemDAO = feedItemDAO;
		this.channelsDAO = channelsDAO;
		this.twitterSubscriptionManager = twitterSubscriptionManager;
		this.rssSubscriptionManager = rssSubscriptionManager;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/ui/{username}/subscriptions/{id}", method=RequestMethod.GET)
	public ModelAndView subscription(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException, UnknownSubscriptionException {
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
		Subscription subscription = subscriptionsDAO.getById(username, id);
		if (subscription == null) {
			throw new RuntimeException("Invalid user");
		}
		
		ModelAndView mv = new ModelAndView("subscription");
		mv.addObject("subscription", subscription);
		mv.addObject("subscriptionSize", feedItemDAO.getSubscriptionFeedItemsCount(subscription.getId()));
		populateFeedItems(subscription, page, mv, "feedItems");		
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/{id}/items", method=RequestMethod.GET)
	public ModelAndView subscriptionItems(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) String format) throws UnknownHostException, MongoException, UnknownSubscriptionException {
		log.info("Items");
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
		Subscription subscription = subscriptionsDAO.getById(username, id);
		
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		if (!Strings.isNullOrEmpty(format) && format.equals("rss")) {
			mv = new ModelAndView(viewFactory.getRssView(subscription.getName(), urlBuilder.getSubscriptionUrl(subscription), ""));
		}
		
		populateFeedItems(subscription, page, mv, "data");		
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/subscriptions/{id}", method=RequestMethod.GET)	
	public ModelAndView subscriptionJson(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException, UnknownSubscriptionException {
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
		Subscription subscription = subscriptionsDAO.getById(username, id);
		
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		populateFeedItems(subscription, page, mv, "data");		
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/subscriptions/{id}/delete")	// TODO should be a HTTP DELETE
	public ModelAndView deleteSubscription(@PathVariable String username, @PathVariable String id) throws UnknownHostException, MongoException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnknownSubscriptionException {
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
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
		
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView()).addObject("data", "ok");
		return mv;
	}
	
	@RequestMapping(value="/ui/{username}/subscriptions/new", method=RequestMethod.GET)
	public ModelAndView newSubscriptionForm(@PathVariable String username) {
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
		ModelAndView mv = new ModelAndView("newSubscription");
		mv.addObject("channels", channelsDAO.getChannels(username));
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions", method=RequestMethod.GET)
	public ModelAndView subscriptions(@PathVariable String username) {
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsDAO.getSubscriptions());
		return mv;
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/feeds", method=RequestMethod.POST)
	public ModelAndView addFeedSubscription(@PathVariable String username, @RequestParam String url, @RequestParam String channel) {
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
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
	public ModelAndView addTwitterTagSubscription(@PathVariable String username, @RequestParam String tag, @RequestParam String channel) {
		log.info("Twitter tag: " + tag);
		twitterSubscriptionManager.requestTagSubscription(tag, channel, username);
		
		return new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
	}
	
	@Timed(timingNotes = "")
	@RequestMapping(value="/{username}/subscriptions/instagram/tags", method=RequestMethod.POST)
	public ModelAndView addInstagramTagSubscription(@PathVariable String username, 
			@RequestParam String tag, @RequestParam String channel) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, UnsupportedEncodingException, HttpFetchException, JSONException {		
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
			@RequestParam String channel) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, UnsupportedEncodingException, HttpFetchException, JSONException {
		LatLong latLong = new LatLong(latitude, longitude);
		
		InstagramGeographySubscription instagramGeographySubscription = instagramSubscriptionManager.requestInstagramGeographySubscription(latLong, radius, channel, username);
		log.info("Saving subscription: " + instagramGeographySubscription);
		subscriptionsDAO.add(instagramGeographySubscription);
				
		return new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
	}

	private void populateFeedItems(Subscription subscription, Integer page, ModelAndView mv, String field) throws UnknownHostException {
		if (page != null) {
			mv.addObject(field, feedItemDAO.getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS, page));
		} else {
			mv.addObject(field, feedItemDAO.getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS));
		}
	}
	
    @ExceptionHandler(UnknownSubscriptionException.class)
    @ResponseStatus(value=org.springframework.http.HttpStatus.NOT_FOUND)
    public ModelAndView unknownSubscriptionException(UnknownSubscriptionException e) {
            return new ModelAndView(viewFactory.getJsonView()).addObject("data", "Not found");
    }

}
