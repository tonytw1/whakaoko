package uk.co.eelpieconsulting.feedlistener.rss;

import com.sun.syndication.feed.synd.SyndFeed;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

public class FeedParserTest {

    @Test
    public void canParseFeedBytesIntoSyndFeed() throws Exception {
        String input = IOUtils.toString(new FileInputStream(this.getClass().getClassLoader().getResource("wcc-news.xml").getFile()));

        SyndFeed syndFeed = new FeedParser().parseSyndFeed(input.getBytes());

        assertEquals("Wellington City Council - News", syndFeed.getTitle());
    }

}
