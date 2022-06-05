package uk.co.eelpieconsulting.feedlistener.rss;

import com.github.kittinunf.result.Result;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeedParserTest {

    private final FeedParser feedParser = new FeedParser();

    @Test
    public void canParseFeedBytesIntoSyndFeed() throws Exception {
        String input = IOUtils.toString(new FileInputStream(this.getClass().getClassLoader().getResource("wcc-news.xml").getFile()));
        System.out.println(input);

        Result<SyndFeed, Exception> result = feedParser.parseSyndFeed(input.getBytes());

        assertEquals("Wellington City Council - News", result.get().getTitle());
    }

    @Test
    public void needToStripNBSPFromMalformedFeeds() throws Exception {
        String input = IOUtils.toString(new FileInputStream(this.getClass().getClassLoader().getResource("vinnies-news.xml").getFile()));

        Result<SyndFeed, Exception> result = feedParser.parseSyndFeed(input.getBytes());

        assertEquals("Latest News - St Vincent de Paul Society Wellington", result.get().getTitle());
    }

    @Test
    public void whatsUpWithCricketWellingtonsFeed() throws Exception {
        String input = IOUtils.toString(new FileInputStream(this.getClass().getClassLoader().getResource("cricketwellington.xml").getFile()));

        Result<SyndFeed, Exception> result = feedParser.parseSyndFeed(input.getBytes());

        assertEquals("Cricket Wellington", result.get().getTitle());
        assertEquals(50, result.get().getEntries().size());


        final Iterator<SyndEntry> feedItemsIterator = result.get().getEntries().iterator();
        SyndEntry next = feedItemsIterator.next();

        assertEquals("Big names return for Firebirds", next.getTitle());
        assertEquals("21 Feb 2020 11:46:19 GMT", next.getPublishedDate().toGMTString());
    }

}
