package uk.co.eelpieconsulting.feedlistener.controllers;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscriptionManager;
import uk.co.eelpieconsulting.feedlistener.instagram.api.InstagramApi;
import uk.co.eelpieconsulting.feedlistener.model.InstagramGeographySubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramTagSubscription;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller;
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener;

import com.mongodb.MongoException;

@Controller
public class SubscriptionsController {
	
	private static Logger log = Logger.getLogger(SubscriptionsController.class);
	
	private SubscriptionsDAO subscriptionsDAO;
	private final RssPoller rssPoller;
	private final TwitterListener twitterListener;
	private final InstagramSubscriptionManager instagramSubscriptionManager;
	private final UrlBuilder urlBuilder;
	private ViewFactory viewFactory;

	@Autowired
	public SubscriptionsController(SubscriptionsDAO subscriptionsDAO, RssPoller rssPoller, TwitterListener twitterListener, 
			InstagramSubscriptionManager instagramSubscriptionManager, UrlBuilder urlBuilder,
			ViewFactory viewFactory) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.rssPoller = rssPoller;
		this.twitterListener = twitterListener;
		this.instagramSubscriptionManager = instagramSubscriptionManager;
		this.urlBuilder = urlBuilder;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/subscriptions/{id}", method=RequestMethod.GET)
	public ModelAndView subscription(@PathVariable String id) {
		final ModelAndView mv = new ModelAndView("subscription");
		mv.addObject("subscription", subscriptionsDAO.getById(id));
		return mv;
	}

	@RequestMapping(value="/subscriptions/{id}/delete")
	public ModelAndView deleteSubscription(@PathVariable String id) throws UnknownHostException, MongoException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {
		final Subscription subscription = subscriptionsDAO.getById(id);
		if (subscription != null) {
			subscriptionsDAO.delete(subscription);
			
			if (subscription.getId().startsWith("twitter")) {
				twitterListener.connect();
			}
			if (subscription.getId().startsWith("instagram")) {
				instagramSubscriptionManager.requestUnsubscribeFrom(((InstagramSubscription) subscription).getSubscriptionId());
			}
		}
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
		return mv;
	}

	@RequestMapping(value="/subscriptions/new", method=RequestMethod.GET)
	public ModelAndView newSubscriptionForm() {
		final ModelAndView mv = new ModelAndView("newSubscription");
		return mv;		
	}

	@RequestMapping(value="/subscriptions/json", method=RequestMethod.GET)
	public ModelAndView subscriptions() {
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsDAO.getSubscriptions());
		return mv;
	}
	
	@RequestMapping(value="/subscriptions/feeds", method=RequestMethod.POST)
	public ModelAndView addFeedSubscription(@RequestParam String url) {		
		subscriptionsDAO.add(new RssSubscription(url));
		rssPoller.run();
		
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
		return mv;
	}
	
	@RequestMapping(value="/subscriptions/twitter/tags", method=RequestMethod.POST)
	public ModelAndView addTwitterTagSubscription(@RequestParam String tag) {		
		subscriptionsDAO.add(new TwitterTagSubscription(tag));
		twitterListener.connect();
		
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
		return mv;
	}
	
	@RequestMapping(value="/subscriptions/instagram/tags", method=RequestMethod.POST)
	public ModelAndView addInstagramTagSubscription(@RequestParam String tag) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, UnsupportedEncodingException, HttpFetchException, JSONException {
		final long subscriptionId = instagramSubscriptionManager.requestInstagramTagSubscription(tag);
		subscriptionsDAO.add(new InstagramTagSubscription(tag, subscriptionId));
		
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
		return mv;
	}
	
	
	@RequestMapping(value="/subscriptions/instagram/geography", method=RequestMethod.POST)
	public ModelAndView addInstagramTagSubscription(@RequestParam double latitude,
			@RequestParam double longitude, 
			@RequestParam int radius) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, UnsupportedEncodingException, HttpFetchException, JSONException {
		final LatLong latLong = new LatLong(latitude, longitude);
		
		final InstagramGeographySubscription instagramGeographySubscription = instagramSubscriptionManager.requestInstagramGeographySubscription(latLong, radius);
		log.info("Saving subscription: " + instagramGeographySubscription);
		subscriptionsDAO.add(instagramGeographySubscription);
		
		new InstagramApi().getRecentMediaForGeography(instagramGeographySubscription.getGeoId(), "d6e2db6c2a95440c84782a41cfaee2be");	// TODO
		
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.getBaseUrl()));
		return mv;
	}

}
