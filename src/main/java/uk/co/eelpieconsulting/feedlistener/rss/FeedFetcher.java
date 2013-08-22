package uk.co.eelpieconsulting.feedlistener.rss;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import uk.co.eelpieconsulting.common.http.HttpFetcher;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

@Component
public class FeedFetcher {
	
	private static Logger log = Logger.getLogger(FeedFetcher.class);
	
	private RssFeedItemMapper rssFeedItemMapper;
	
	@Autowired
	public FeedFetcher(RssFeedItemMapper rssFeedItemMapper) {
		this.rssFeedItemMapper = rssFeedItemMapper;
	}

	@SuppressWarnings("unchecked")
	public FetchedFeed fetchFeed(String url) {
		final List<FeedItem> feedItems = new ArrayList<FeedItem>();
    	final SyndFeed syndfeed = loadSyndFeedWithFeedFetcher(url);
    	if (syndfeed == null) {
    		log.warn("Could not load syndfeed from url: " + url + ". Returning empty list of items");
    		return null;
    	}
    	
        Iterator<SyndEntry> feedItemsIterator = syndfeed.getEntries().iterator();
        while (feedItemsIterator.hasNext()) {        	
        	final SyndEntry syndEntry = (SyndEntry) feedItemsIterator.next();
        	feedItems.add(rssFeedItemMapper.createFeedItemFrom(syndEntry));
        }
        
        return new FetchedFeed(syndfeed.getTitle(), feedItems);
	}
	
	private SyndFeed loadSyndFeedWithFeedFetcher(String feedUrl) {
		log.info("Loading SyndFeed from url: " + feedUrl);
		try {
			final HttpFetcher httpFetcher = new HttpFetcher();
			final byte[] bytes = httpFetcher.getBytes(feedUrl);
			final SyndFeedInput feedInput = new SyndFeedInput();
			return feedInput.build(new InputSource(new ByteArrayInputStream(bytes)));
			
		} catch (Exception e) {
			log.warn("Error while fetching feed: " + e.getMessage());
		}
		return null;
	}
	
}
