package uk.co.eelpieconsulting.feedlistener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;

@Controller
public class HomepageController {
	
	private SubscriptionsDAO subscriptionsDAO;
	
	@Autowired
	public HomepageController(SubscriptionsDAO subscriptionsDAO) {
		this.subscriptionsDAO = subscriptionsDAO;
	}
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView homepage() {
		final ModelAndView mv = new ModelAndView("homepage");
		mv.addObject("subscriptions", subscriptionsDAO.getSubscriptions());
		return mv;
	}
}
