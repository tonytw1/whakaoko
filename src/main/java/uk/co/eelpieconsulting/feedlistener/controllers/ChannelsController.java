package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.IdBuilder;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.common.collect.Maps;
import com.mongodb.MongoException;

@Controller
public class ChannelsController {
	
	private static final int MAX_FEED_ITEMS = 20;
			
	private final ChannelsDAO channelsDAO;
	private final SubscriptionsDAO subscriptionsDAO;
	private final UrlBuilder urlBuilder;
	private final FeedItemDAO feedItemDAO;
	private final IdBuilder idBuilder;
	private final ViewFactory viewFactory;
	
	@Autowired
	public ChannelsController(ChannelsDAO channelsDAO, SubscriptionsDAO subscriptionsDAO, UrlBuilder urlBuilder, 
			FeedItemDAO feedItemDAO, IdBuilder idBuilder, ViewFactory viewFactory) {
		this.channelsDAO = channelsDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.urlBuilder = urlBuilder;
		this.feedItemDAO = feedItemDAO;
		this.idBuilder = idBuilder;
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
	public ModelAndView channel(@PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException {
		final Channel channel = channelsDAO.getById(id);

		final ModelAndView mv = new ModelAndView("channel");
		mv.addObject("channel", channel);
		
		final List<Subscription> subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.getId());
		mv.addObject("subscriptions", subscriptionsForChannel);
		
		if (!subscriptionsForChannel.isEmpty()) {
			mv.addObject("inboxSize", feedItemDAO.getChannelFeedItemsCount(channel.getId()));
			
			if (page != null) {
				mv.addObject("inbox", feedItemDAO.getChannelFeedItems(channel.getId(), 20, page));
			} else {
				mv.addObject("inbox", feedItemDAO.getChannelFeedItems(channel.getId(), 20));
			}
			
			final Map<String, Long> subscriptionCounts = Maps.newHashMap();
			for (Subscription subscription : subscriptionsForChannel) {
				subscriptionCounts.put(subscription.getId(), feedItemDAO.getSubscriptionFeedItemsCount(subscription.getId()));	// TODO slow on channels with many subscriptions - cache or index?
			}
			mv.addObject("subscriptionCounts", subscriptionCounts);
		}
		return mv;
	}
	
	@RequestMapping(value="/channels/{id}/rss", method=RequestMethod.GET)
	public ModelAndView channelRss(@PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException {
		final Channel channel = channelsDAO.getById(id);
		
		final ModelAndView mv = new ModelAndView(viewFactory.getRssView(channel.getName() + " items", 
				urlBuilder.getChannelUrl(channel.getId()), 
				channel.getName() + " items"));
				
		if (page != null) {
			mv.addObject("data", feedItemDAO.getChannelFeedItems(channel.getId(), MAX_FEED_ITEMS, page));
		} else {
			mv.addObject("data", feedItemDAO.getChannelFeedItems(channel.getId(), MAX_FEED_ITEMS));
		}
		
		mv.addObject("data", feedItemDAO.getChannelFeedItems(channel.getId(), MAX_FEED_ITEMS));
		return mv;
	}
	
	@RequestMapping(value="/channels/{id}/json", method=RequestMethod.GET)
	public ModelAndView channelJson(@PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException {
		final Channel channel = channelsDAO.getById(id);
		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		
		if (page != null) {
			mv.addObject("data", feedItemDAO.getChannelFeedItems(channel.getId(), MAX_FEED_ITEMS, page));
		} else {
			mv.addObject("data", feedItemDAO.getChannelFeedItems(channel.getId(), MAX_FEED_ITEMS));
		}
				
		return mv;
	}
	
	@RequestMapping(value="/channels/new", method=RequestMethod.GET)
	public ModelAndView newChannelForm() {
		final ModelAndView mv = new ModelAndView("newChannel");
		return mv;		
	}
	
	@RequestMapping(value="/channels", method=RequestMethod.POST)
	public ModelAndView addChannel(@RequestParam String name) {		
		channelsDAO.add(new Channel(idBuilder.makeIdFor(name), name));
		
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.getChannelsUrl()));
		return mv;
	}
	
}
