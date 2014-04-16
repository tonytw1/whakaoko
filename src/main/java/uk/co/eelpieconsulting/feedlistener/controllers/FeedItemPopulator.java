package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.mongodb.MongoException;

import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class FeedItemPopulator {
	
	private static int MAX_FEED_ITEMS = 20;

	private final FeedItemDAO feedItemDAO;
	
	@Autowired
	public FeedItemPopulator(FeedItemDAO feedItemDAO) {
		this.feedItemDAO = feedItemDAO;
	}
	
	public void populateFeedItems(Subscription subscription, Integer page, ModelAndView mv, String field) throws UnknownHostException {
		if (page != null) {
			mv.addObject(field, feedItemDAO.getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS, page));
		} else {
			mv.addObject(field, feedItemDAO.getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS));
		}
	}
	
	public void populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field) throws UnknownHostException, MongoException {
		populateFeedItems(username, channel, page, mv, field, MAX_FEED_ITEMS);
	}
	
	public void populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field, Integer pageSize) throws UnknownHostException, MongoException {		
		final int pageSizeToUse = pageSize != null ? pageSize : MAX_FEED_ITEMS;
		final int pageToUse = (page != null && page > 0) ? page : 1;
		if (pageSizeToUse > MAX_FEED_ITEMS) {
			throw new RuntimeException("Too many records requested");	// TODO use correct exception.
		}		
		mv.addObject(field, feedItemDAO.getChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username));
	}
	
}