package uk.co.eelpieconsulting.feedlistener.rss;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;
import uk.co.eelpieconsulting.feedlistener.rss.FeedFetcher;
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller;

public class RssPollerTest {
	
	@Test
	public void runPoller() throws Exception {
		SubscriptionsDAO subscriptionsDAO = new SubscriptionsDAO();
		FeedFetcher feedFetcher = new FeedFetcher();
		FeedItemDAO feedItemDAO = new FeedItemDAO();
		RssPoller poller = new RssPoller(subscriptionsDAO, feedFetcher, feedItemDAO);
		
		subscriptionsDAO.addSubscription(new RssSubscription("http://wellington.gen.nz/rss"));
		subscriptionsDAO.addSubscription(new RssSubscription("http://guardian.co.uk/rss"));
		
		poller.run();
		
		ImmutableList<FeedItem> all = feedItemDAO.getAll();
		for (FeedItem feedItem : all) {
			System.out.println(feedItem.getTitle() + ": " + feedItem.getDate());
		}
	}
	
}
