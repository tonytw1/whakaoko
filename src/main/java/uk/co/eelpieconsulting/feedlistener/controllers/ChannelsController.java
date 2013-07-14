package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.Channel;

import com.mongodb.MongoException;

@Controller
public class ChannelsController {
			
	private final ChannelsDAO channelsDAO;
	private final SubscriptionsDAO subscriptionsDAO;
	private final UrlBuilder urlBuilder;
	private final ViewFactory viewFactory;
	
	@Autowired
	public ChannelsController(ChannelsDAO channelsDAO, SubscriptionsDAO subscriptionsDAO, UrlBuilder urlBuilder, ViewFactory viewFactory) {
		this.channelsDAO = channelsDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.urlBuilder = urlBuilder;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/channels", method=RequestMethod.GET)
	public ModelAndView channels() {
		final ModelAndView mv = new ModelAndView("channels");
		mv.addObject("channels", channelsDAO.getChannels());
		return mv;
	}

	@RequestMapping(value="/channels/json", method=RequestMethod.GET)
	public ModelAndView channelsJson() {
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", channelsDAO.getChannels());
		return mv;
	}
	
	@RequestMapping(value="/channels/{id}", method=RequestMethod.GET)
	public ModelAndView subscription(@PathVariable String id) throws UnknownHostException, MongoException {
		final Channel channel = channelsDAO.getById(id);

		final ModelAndView mv = new ModelAndView("channel");
		mv.addObject("channel", channel);
		mv.addObject("subscriptions", subscriptionsDAO.getSubscriptionsForChannel(channel.getId()));
		return mv;
	}
	
	@RequestMapping(value="/channels/new", method=RequestMethod.GET)
	public ModelAndView newChannelForm() {
		final ModelAndView mv = new ModelAndView("newChannel");
		return mv;		
	}
	
	@RequestMapping(value="/channels", method=RequestMethod.POST)
	public ModelAndView addChannel(@RequestParam String name) {
		channelsDAO.add(new Channel(name, name));	// TODO proper ids.
		
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.getChannelsUrl()));
		return mv;
	}
	
}
