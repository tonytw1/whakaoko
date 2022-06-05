package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.google.common.base.Strings;
import com.rometools.rome.feed.synd.SyndEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.rss.RssFeedItemBodyExtractor;

@Component
public class BodyHtmlImageExtractor {

    private static Logger log = LogManager.getLogger(BodyHtmlImageExtractor.class);

    private final RssFeedItemBodyExtractor rssFeedItemBodyExtractor;

    @Autowired
    public BodyHtmlImageExtractor(RssFeedItemBodyExtractor rssFeedItemBodyExtractor) {
        this.rssFeedItemBodyExtractor = rssFeedItemBodyExtractor;
    }

    public String extractImageFrom(SyndEntry item) {
        final String itemBody = rssFeedItemBodyExtractor.extractBody(item);
        if (!Strings.isNullOrEmpty(itemBody)) {
            String imageSrc = extractImage(itemBody);
            if (!Strings.isNullOrEmpty(imageSrc)) {
                return imageSrc;
            }
        }
        return null;
    }

    private String extractImage(final String itemBody) {
        try {
            final Parser parser = new Parser();
            parser.setInputHTML(itemBody);
            NodeFilter tagNameFilter = new TagNameFilter("img");
            NodeList imageNodes = parser.extractAllNodesThatMatch(tagNameFilter);
            log.debug("Found images: " + imageNodes.size());
            return extractFirstImage(imageNodes);

        } catch (ParserException e) {
            log.warn("Failed to parse item body for images", e);
        }
        return null;
    }

    private String extractFirstImage(NodeList imageNodes) {
        if (imageNodes.size() == 0) {
            return null;
        }

        final Tag imageTag = (Tag) imageNodes.elementAt(0);
        final String imageSrc = imageTag.getAttribute("src");
        log.debug("Found first image: " + imageTag.toHtml() + ", " + imageSrc);
        return imageSrc;
    }

}
