package uk.co.eelpieconsulting.feedlistener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

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
	public ModelAndView homepage() {
		final ModelAndView mv = new ModelAndView("homepage");
		mv.addObject("subscriptions", subscriptionsDAO.getSubscriptions());
		
		final ImmutableList<FeedItem> all = feedItemDAO.getAll();
		mv.addObject("inboxSize", all.size());
		mv.addObject("inbox", Lists.newArrayList(Iterables.limit(all, 10)));
		return mv;
	}
}
