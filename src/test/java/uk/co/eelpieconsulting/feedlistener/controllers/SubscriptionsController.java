package uk.co.eelpieconsulting.feedlistener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;

@Controller
public class SubscriptionsController {
	
	private SubscriptionsDAO subscriptionsDAO;
	private ViewFactory viewFactory;
	
	@Autowired
	public SubscriptionsController(SubscriptionsDAO subscriptionsDAO, ViewFactory viewFactory) {
		this.subscriptionsDAO = subscriptionsDAO;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/subscriptions")
	public ModelAndView subscriptions() {
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsDAO.getSubscriptions());
		return mv;
	}

}
