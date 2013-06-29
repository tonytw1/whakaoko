package uk.co.eelpieconsulting.feedlistener.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;

@Controller
public class InboxController {
	
	private final FeedItemDAO feedItemDAO;
	private final ViewFactory viewFactory;
	
	@Autowired
	public InboxController(FeedItemDAO feedItemDAO, ViewFactory viewFactory) {
		this.feedItemDAO = feedItemDAO;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping("/inbox/json")
	public ModelAndView inbox() {
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", feedItemDAO.getAll());
		return mv;
	}
	
	@RequestMapping("/inbox/rss")
	public ModelAndView inboxRss() {
		final ModelAndView mv = new ModelAndView(viewFactory.getRssView("Inbox", "http://localhost:9090/inbox", "Inbox items"));	// TODO url builder
		mv.addObject("data", feedItemDAO.getAll());
		return mv;
	}

}
