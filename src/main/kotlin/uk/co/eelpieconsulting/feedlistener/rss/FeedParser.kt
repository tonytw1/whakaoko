package uk.co.eelpieconsulting.feedlistener.rss;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.ParsingFeedException;
import com.sun.syndication.io.SyndFeedInput;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class FeedParser {

    public SyndFeed parseSyndFeed(byte[] bytes) throws FeedException {
        final byte[] cleaned = cleanLeadingWhitespace(bytes);

        try {
            return parse(cleaned);

        } catch (ParsingFeedException p) {
            if (p.getMessage().contains("The entity \"nbsp\" was referenced, but not declared.")) {
                byte[] withOutNbsp = new String(bytes).replaceAll("\\&nbsp;", " ").getBytes();
                return parse(withOutNbsp);

            } else if (p.getMessage().contains("Invalid XML: Error on line 1: Content is not allowed in prolog")) {
                byte[] withUtf8 = new String(bytes).replaceAll("utf-16", "utf-8").getBytes();
                return parse(withUtf8);

            } else {
                throw (p);
            }
        }
    }

    private SyndFeed parse(byte[] bytes) throws FeedException {
        final InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        InputSource inputStream = new InputSource(byteArrayInputStream);
        try {
            SyndFeedInput syndFeedInput = new SyndFeedInput(false);
            final SyndFeed syndFeed = syndFeedInput.build(inputStream);
            IOUtils.closeQuietly(byteArrayInputStream);
            return syndFeed;

        } catch (FeedException e) {
            IOUtils.closeQuietly(byteArrayInputStream);
            throw (e);
        }
    }

    private byte[] cleanLeadingWhitespace(byte[] bytes) {
        try {
            String string = new String(bytes);
            if (string.trim().equals(string)) {
                return bytes;
            }
            return string.trim().getBytes();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

