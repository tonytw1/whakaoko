package uk.co.eelpieconsulting.feedlistener.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class FeedParserTest {

    @Test
    public void canParseFeedBytesIntoSyndFeed() throws Exception {
        String input = IOUtils.toString(new FileInputStream(this.getClass().getClassLoader().getResource("wcc-news.xml").getFile()));

        SyndFeed syndFeed = new FeedParser().parseSyndFeed(input.getBytes());

        assertEquals("Wellington City Council - News", syndFeed.getTitle());
    }

    @Test
    public void needToStripNBSPFromMalformedFeeds() throws Exception {
        String input = IOUtils.toString(new FileInputStream(this.getClass().getClassLoader().getResource("vinnies-news.xml").getFile()));

        SyndFeed syndFeed = new FeedParser().parseSyndFeed(input.getBytes());

        assertEquals("Latest News - St Vincent de Paul Society Wellington", syndFeed.getTitle());
    }

    @Test
    public void whatsUpWithCricketWellingtonsFeed() throws Exception {
        String input = IOUtils.toString(new FileInputStream(this.getClass().getClassLoader().getResource("cricketwellington.xml").getFile()));

        SyndFeed syndFeed = new FeedParser().parseSyndFeed(input.getBytes());

        assertEquals("Cricket Wellington", syndFeed.getTitle());
        assertEquals(50, syndFeed.getEntries().size());


        final Iterator<SyndEntry> feedItemsIterator = syndFeed.getEntries().iterator();
        SyndEntry next = feedItemsIterator.next();

        assertEquals("Big names return for Firebirds", next.getTitle());
        assertEquals("21 Feb 2020 11:46:19 GMT", next.getPublishedDate().toGMTString());
    }

}
