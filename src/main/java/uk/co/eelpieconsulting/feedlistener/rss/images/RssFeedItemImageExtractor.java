package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RssFeedItemImageExtractor {

    private final static Logger log = Logger.getLogger(RssFeedItemImageExtractor.class);

    private final BodyHtmlImageExtractor bodyHtmlImageExtractor;
    private final MediaModuleImageExtractor mediaModuleImageExtractor;

    @Autowired
    public RssFeedItemImageExtractor(BodyHtmlImageExtractor bodyHtmlImageExtractor,
                                     MediaModuleImageExtractor mediaModuleImageExtractor) {
        this.bodyHtmlImageExtractor = bodyHtmlImageExtractor;
        this.mediaModuleImageExtractor = mediaModuleImageExtractor;
    }

    public String extractImageFrom(SyndEntry item) {
        // Look for an RSS media module image; if that fails try to extract an image from the HTML body.
        String mediaModuleImage = mediaModuleImageExtractor.extractImageFromMediaModule(item);
        if (mediaModuleImage != null) {
            return mediaModuleImage;
        }

        String bodyHtmlImage = bodyHtmlImageExtractor.extractImageFrom(item);
        if (bodyHtmlImage != null) {
            return bodyHtmlImage;
        }

        log.debug("No suitable media element image seen");
        return null;
    }

}
