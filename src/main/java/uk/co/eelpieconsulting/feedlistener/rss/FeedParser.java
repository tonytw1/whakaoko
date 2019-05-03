package uk.co.eelpieconsulting.feedlistener.rss;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class FeedParser {

    public SyndFeed parseSyndFeed(byte[] bytes) throws FeedException {
        final InputStream byteArrayInputStream = cleanLeadingWhitespace(bytes);
        InputSource inputStream = new InputSource(byteArrayInputStream);
        final SyndFeed syndFeed = new SyndFeedInput().build(inputStream);
        IOUtils.closeQuietly(byteArrayInputStream);

        return syndFeed;
    }

    private ByteArrayInputStream cleanLeadingWhitespace(byte[] bytes) {
        try {
            String string = new String(bytes);
            if (string.trim().equals(string)) {
                return new ByteArrayInputStream(bytes);
            }
            return new ByteArrayInputStream(string.trim().getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

