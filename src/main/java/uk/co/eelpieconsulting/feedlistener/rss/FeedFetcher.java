package uk.co.eelpieconsulting.feedlistener.rss;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import uk.co.eelpieconsulting.common.http.HttpFetcher;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

@Component
public class FeedFetcher {
	
	private static Logger log = Logger.getLogger(FeedFetcher.class);
	
	private RssFeedItemMapper rssFeedItemMapper;

	private HttpFetcher httpFetcher;
	
	@Autowired
	public FeedFetcher(RssFeedItemMapper rssFeedItemMapper) {
		this.rssFeedItemMapper = rssFeedItemMapper;
		this.httpFetcher = new HttpFetcher();
	}

	public FetchedFeed fetchFeed(String url) {
    	final SyndFeed syndfeed = loadSyndFeedWithFeedFetcher(url);
    	if (syndfeed == null) {
    		log.warn("Could not load syndfeed from url: " + url + ". Returning empty list of items");
    		return null;
    	}
    	
    	final List<FeedItem> feedItems = getFeedItemsFrom(syndfeed);        
        return new FetchedFeed(syndfeed.getTitle(), feedItems);
	}
	
	private SyndFeed loadSyndFeedWithFeedFetcher(String feedUrl) {
		log.info("Loading SyndFeed from url: " + feedUrl);
		try {
			InputStream byteArrayInputStream = new ByteArrayInputStream(httpFetcher.getBytes(feedUrl));
			final SyndFeed syndFeed = new SyndFeedInput().build(new InputSource(byteArrayInputStream));
			IOUtils.closeQuietly(byteArrayInputStream);			
			return syndFeed;
			
		} catch (Exception e) {
			log.warn("Error while fetching feed: " + e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<FeedItem> getFeedItemsFrom(final SyndFeed syndfeed) {
		final List<FeedItem> feedItems = Lists.newArrayList();
        final Iterator<SyndEntry> feedItemsIterator = syndfeed.getEntries().iterator();
        while (feedItemsIterator.hasNext()) {        	
        	final SyndEntry syndEntry = (SyndEntry) feedItemsIterator.next();
        	feedItems.add(rssFeedItemMapper.createFeedItemFrom(syndEntry));
        }
		return feedItems;
	}
	
}
