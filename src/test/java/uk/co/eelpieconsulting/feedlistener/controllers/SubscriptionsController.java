package uk.co.eelpieconsulting.feedlistener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller;
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener;

@Controller
public class SubscriptionsController {
	
	private SubscriptionsDAO subscriptionsDAO;
	private final RssPoller rssPoller;
	private final TwitterListener twitterListener;
	private ViewFactory viewFactory;
	
	@Autowired
	public SubscriptionsController(SubscriptionsDAO subscriptionsDAO, RssPoller rssPoller, TwitterListener twitterListener, ViewFactory viewFactory) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.rssPoller = rssPoller;
		this.twitterListener = twitterListener;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/subscriptions", method=RequestMethod.GET)
	public ModelAndView subscriptions() {
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsDAO.getSubscriptions());
		return mv;
	}
	
	@RequestMapping(value="/subscriptions/feeds", method=RequestMethod.POST)
	public ModelAndView addFeedSubscription(@RequestParam String url) {		
		subscriptionsDAO.add(new RssSubscription(url));
		rssPoller.run();
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsDAO.getSubscriptions());
		return mv;
	}
	
	@RequestMapping(value="/subscriptions/twitter/tags", method=RequestMethod.POST)
	public ModelAndView addTwitterTagSubscription(@RequestParam String tag) {		
		subscriptionsDAO.add(new TwitterTagSubscription(tag));
		twitterListener.connect();
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsDAO.getSubscriptions());
		return mv;
	}

}
