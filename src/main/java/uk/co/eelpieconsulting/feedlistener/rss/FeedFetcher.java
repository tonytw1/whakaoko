package uk.co.eelpieconsulting.feedlistener.rss;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpFetcher;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import java.util.Iterator;
import java.util.List;

@Component
public class FeedFetcher {

    private final static Logger log = Logger.getLogger(FeedFetcher.class);

    private RssFeedItemMapper rssFeedItemMapper;
    private HttpFetcher httpFetcher;
    private FeedParser feedParser;

    @Autowired
    public FeedFetcher(RssFeedItemMapper rssFeedItemMapper, HttpFetcher httpFetcher, FeedParser feedParser) {
        this.rssFeedItemMapper = rssFeedItemMapper;
        this.httpFetcher = httpFetcher;
        this.feedParser = feedParser;
    }

    public FetchedFeed fetchFeed(String url) throws HttpFetchException, FeedException {
        final SyndFeed syndfeed = loadSyndFeedWithFeedFetcher(url);
        final List<FeedItem> feedItems = getFeedItemsFrom(syndfeed);
        return new FetchedFeed(syndfeed.getTitle(), feedItems, null);   // TODO we'd like to be able to capture etag and other caching related headers
    }

    private SyndFeed loadSyndFeedWithFeedFetcher(String feedUrl) throws HttpFetchException, FeedException {
        log.info("Loading SyndFeed from url: " + feedUrl + " using http fetcher: " + httpFetcher.hashCode());
        return feedParser.parseSyndFeed(httpFetcher.getBytes(feedUrl));
    }

    @SuppressWarnings("unchecked")
    private List<FeedItem> getFeedItemsFrom(final SyndFeed syndfeed) {
        final List<FeedItem> feedItems = Lists.newArrayList();
        final Iterator<SyndEntry> feedItemsIterator = syndfeed.getEntries().iterator();
        while (feedItemsIterator.hasNext()) {
            final SyndEntry syndEntry = feedItemsIterator.next();
            feedItems.add(rssFeedItemMapper.createFeedItemFrom(syndEntry));
        }
        return feedItems;
    }

}
