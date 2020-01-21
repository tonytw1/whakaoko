package uk.co.eelpieconsulting.feedlistener.controllers;

import com.google.common.base.Strings;
import com.mongodb.MongoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

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
	
	public void populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field, String q) throws UnknownHostException, MongoException {
		populateFeedItems(username, channel, page, mv, field, MAX_FEED_ITEMS, q);
	}
	
	void populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field, Integer pageSize, String q) throws UnknownHostException, MongoException {
		final int pageSizeToUse = pageSize != null ? pageSize : MAX_FEED_ITEMS;
		final int pageToUse = (page != null && page > 0) ? page : 1;
		if (pageSizeToUse > MAX_FEED_ITEMS) {
			throw new RuntimeException("Too many records requested");	// TODO use correct exception.
		}

		List<FeedItem> feedItems;
		if (!Strings.isNullOrEmpty(q)) {
			feedItems = feedItemDAO.searchChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username, q);
		} else {
			feedItems = feedItemDAO.getChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username);
		}

		List<FeedItem> geotagged = feedItems.stream().filter(FeedItem::isGeoTagged).collect(Collectors.toList());
		mv.addObject("geotagged", geotagged);

		mv.addObject(field, feedItems);
	}
	
}