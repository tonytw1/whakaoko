package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.rometools.rome.feed.synd.SyndEntry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

@Component
public class RssFeedItemImageExtractor {

    private final static Logger log = LogManager.getLogger(RssFeedItemImageExtractor.class);

    private final Set<String> BLOCKED_URL_SNIPPETS = Set.of("http://stats.wordpress.com", "gravatar.com/avatar", "share_save_171_16");

    private final BodyHtmlImageExtractor bodyHtmlImageExtractor;
    private final MediaModuleImageExtractor mediaModuleImageExtractor;
    private final Counter rssImage;
    private final Counter rssImageMediaModule;
    private final Counter rssImageBodyHtml;

    @Autowired
    public RssFeedItemImageExtractor(BodyHtmlImageExtractor bodyHtmlImageExtractor,
                                     MediaModuleImageExtractor mediaModuleImageExtractor,
                                     MeterRegistry meterRegistry) {
        this.bodyHtmlImageExtractor = bodyHtmlImageExtractor;
        this.mediaModuleImageExtractor = mediaModuleImageExtractor;

        this.rssImage = meterRegistry.counter("rss_image");
        this.rssImageMediaModule = meterRegistry.counter("rss_image_media_module");
        this.rssImageBodyHtml = meterRegistry.counter("rss_image_body_html");
    }

    public String extractImageFrom(SyndEntry item) {
        rssImage.increment();

        // Look for an RSS media module image; if that fails try to extract an image from the HTML body.
        String mediaModuleImage = mediaModuleImageExtractor.extractImageFromMediaModule(item);
        if (mediaModuleImage != null) {
            rssImageMediaModule.increment();
            return mediaModuleImage;
        }

        String bodyHtmlImage = bodyHtmlImageExtractor.extractImageFrom(item);
        if (bodyHtmlImage != null) {
            // Attempt to expand relative image paths to fully qualified urls.
            String fullyQualifiedImagePath = ensureFullyQualifiedUrl(bodyHtmlImage, item.getLink());
            if (fullyQualifiedImagePath != null && !isBlockListedImageUrl(fullyQualifiedImagePath)) { // TODO specific to HTML only?
                rssImageBodyHtml.increment();
                return fullyQualifiedImagePath;
            }
        }

        log.debug("No suitable media element image seen");
        return null;
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
        return BLOCKED_URL_SNIPPETS.stream().anyMatch(url::contains);
    }

}
