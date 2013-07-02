package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.common.collect.Maps;
import com.mongodb.MongoException;

@Controller
public class HomepageController {
	
	private final SubscriptionsDAO subscriptionsDAO;
	private final FeedItemDAO feedItemDAO;
	
	@Autowired
	public HomepageController(SubscriptionsDAO subscriptionsDAO, FeedItemDAO feedItemDAO) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.feedItemDAO = feedItemDAO;
	}
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView homepage() throws UnknownHostException, MongoException {
		final ModelAndView mv = new ModelAndView("homepage");
		final List<Subscription> subscriptions = subscriptionsDAO.getSubscriptions();

		mv.addObject("subscriptions", subscriptions);
		mv.addObject("inboxSize", feedItemDAO.getAllCount());
		mv.addObject("inbox", feedItemDAO.getInbox(20));
		
		final Map<String, Long> subscriptionCounts = Maps.newHashMap();
		for (Subscription subscription : subscriptions) {
			subscriptionCounts.put(subscription.getId(), feedItemDAO.getSubscriptionFeedItemsCount(subscription.getId()));
		}
		mv.addObject("subscriptionCounts", subscriptionCounts);
		
		return mv;
	}
}
