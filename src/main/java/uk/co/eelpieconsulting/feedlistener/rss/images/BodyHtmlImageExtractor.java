package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.rss.RssFeedItemBodyExtractor;
import com.sun.syndication.feed.synd.SyndEntry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

@Component
public class BodyHtmlImageExtractor {

    private static Logger log = Logger.getLogger(BodyHtmlImageExtractor.class);

    private final Set<String> blockedUrlSnippets = Set.of("http://stats.wordpress.com", "gravatar.com/avatar", "share_save_171_16");

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
                imageSrc = ensureFullyQualifiedUrl(imageSrc, item.getLink());   // TODO push up?
                if (imageSrc != null && !isBlockListedImageUrl(imageSrc)) {     // TODO push up?
                    return imageSrc;
                }
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

    private String ensureFullyQualifiedUrl(final String imageSrc, String itemUrl) {
        if (imageSrc.startsWith("/")) {
            log.debug("Image src looks like a relative url; attempting to fully qualify");
            try {
                final URL itemUrlUrl = new URL(itemUrl);
                final String fullyQualifiedImageSrc = itemUrlUrl.getProtocol() + "://" + itemUrlUrl.getHost() + imageSrc;

                final URL fullyQualifiedImageSrcUrl = new URL(fullyQualifiedImageSrc);
                log.debug("Referenced from root image src resolved to fully qualified url: " + fullyQualifiedImageSrcUrl.toExternalForm());
                return fullyQualifiedImageSrcUrl.toExternalForm();

            } catch (MalformedURLException e) {
                log.warn("Item base url or generated referenced from root image src url is not well formed; returning null");
                return null;
            }

        } else {
            try {
                URL imageSrcUrl = new URL(imageSrc);
                return imageSrcUrl.toExternalForm();

            } catch (MalformedURLException e) {
                log.debug("Returning null fully qualified image url for: " + imageSrc + " / " + itemUrl);
                return null;
            }
        }
    }

    private boolean isBlockListedImageUrl(String url) {
        return blockedUrlSnippets.stream().anyMatch(url::contains);
    }
}
