package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.IdBuilder;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.mongodb.MongoException;

@Controller
public class ChannelsController {
	
	private static Logger log = Logger.getLogger(ChannelsController.class);
	
	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAXIMUM_PAGE_SIZE = 100;
			
	private final UsersDAO usersDAO;
	private final ChannelsDAO channelsDAO;
	private final SubscriptionsDAO subscriptionsDAO;
	private final FeedItemDAO feedItemDAO;
	private final IdBuilder idBuilder;
	private final UrlBuilder urlBuilder;
	private final ViewFactory viewFactory;
	
	@Autowired
	public ChannelsController(UsersDAO usersDAO, ChannelsDAO channelsDAO, SubscriptionsDAO subscriptionsDAO, 
			FeedItemDAO feedItemDAO, IdBuilder idBuilder, UrlBuilder urlBuilder, ViewFactory viewFactory) {
		this.usersDAO = usersDAO;
		this.channelsDAO = channelsDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.feedItemDAO = feedItemDAO;
		this.idBuilder = idBuilder;
		this.urlBuilder = urlBuilder;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/{username}/channels", method=RequestMethod.GET)
	public ModelAndView channelsJson(@PathVariable String username) throws UnknownUserException {
		usersDAO.getByUsername(username);
		
		log.info("Channels for user: " + username);
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", channelsDAO.getChannels(username));
		return mv;
	}
	
	@RequestMapping(value="/ui/{username}/channels/{id}", method=RequestMethod.GET)
	public ModelAndView channel(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException {
		final Channel channel = channelsDAO.getById(username, id);

		final ModelAndView mv = new ModelAndView("channel");
		mv.addObject("channel", channel);
		
		final List<Subscription> subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.getId());
		mv.addObject("subscriptions", subscriptionsForChannel);
		
		if (!subscriptionsForChannel.isEmpty()) {
			mv.addObject("inboxSize", feedItemDAO.getChannelFeedItemsCount(channel.getId()));
			
			
			if (page != null) {
				mv.addObject("inbox", feedItemDAO.getChannelFeedItems(channel.getId(), DEFAULT_PAGE_SIZE, page));
			} else {
				mv.addObject("inbox", feedItemDAO.getChannelFeedItems(channel.getId(), DEFAULT_PAGE_SIZE));
			}
			
			final Map<String, Long> subscriptionCounts = Maps.newHashMap();
			for (Subscription subscription : subscriptionsForChannel) {
				subscriptionCounts.put(subscription.getId(), feedItemDAO.getSubscriptionFeedItemsCount(subscription.getId()));	// TODO slow on channels with many subscriptions - cache or index?
			}
			mv.addObject("subscriptionCounts", subscriptionCounts);
		}
		return mv;
	}
	
	@RequestMapping(value="/{username}/channels/{id}", method=RequestMethod.GET)
	public ModelAndView channel(@PathVariable String username, @PathVariable String id) throws UnknownHostException, MongoException, UnknownUserException {		
		usersDAO.getByUsername(username);
		
		final Channel channel = channelsDAO.getById(username, id);
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", channel);		
		return mv;
	}
	
	@RequestMapping(value="/{username}/channels/{id}/subscriptions", method=RequestMethod.GET)
	public ModelAndView channelSubscriptions(@PathVariable String username, @PathVariable String id) throws UnknownHostException, MongoException, UnknownUserException {
		usersDAO.getByUsername(username);
		
		final Channel channel = channelsDAO.getById(username, id);
		final List<Subscription> subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.getId());
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", subscriptionsForChannel);		
		return mv;
	}
	
	@RequestMapping(value="/{username}/channels/{id}/items", method=RequestMethod.GET)
	public ModelAndView channelJson(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) Integer pageSize,
			@RequestParam(required=false) String format
	) throws UnknownHostException, MongoException, UnknownUserException {
		usersDAO.getByUsername(username);
		
		final Channel channel = channelsDAO.getById(username, id);
		
		ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		if (!Strings.isNullOrEmpty(format) && format.equals("rss")) {
			mv = new ModelAndView(viewFactory.getRssView(channel.getName(), urlBuilder.getChannelUrl(channel), ""));
		}
		
		int pageSizeToUse = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
		int pageToUse = (page != null && page > 0) ? page : 1;
		if (pageSizeToUse > MAXIMUM_PAGE_SIZE) {
			throw new RuntimeException("Too many records requested");	// TODO use correct exception.
		}
		
		mv.addObject("data", feedItemDAO.getChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse));	
		return mv;
	}
	
	@RequestMapping(value="/ui/channels/new", method=RequestMethod.GET)
	public ModelAndView newChannelForm() {
		return new ModelAndView("newChannel");		
	}
	
	@RequestMapping(value="/{username}/channels", method=RequestMethod.POST)
	public ModelAndView addChannel(@PathVariable String username, @RequestParam String name) throws UnknownUserException {
		usersDAO.getByUsername(username);
		
		channelsDAO.add(username, new Channel(idBuilder.makeIdFor(name), name, username));
		
		return new ModelAndView(viewFactory.getJsonView()).addObject("data", "ok");
	}
	
}
