package uk.co.eelpieconsulting.feedlistener.rss;

import com.google.common.base.Strings;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

@Component
public class RssFeedItemImageExtractor {

    private static Logger log = Logger.getLogger(RssFeedItemImageExtractor.class);

    private final Set<String> blockedUrlSnippets = Set.of("http://stats.wordpress.com", "gravatar.com/avatar", "share_save_171_16");

    private final RssFeedItemBodyExtractor rssFeedItemBodyExtractor;
    private final HtmlImageExtractor htmlImageExtractor;

    @Autowired
    public RssFeedItemImageExtractor(RssFeedItemBodyExtractor rssFeedItemBodyExtractor, HtmlImageExtractor htmlImageExtractor) {
        this.rssFeedItemBodyExtractor = rssFeedItemBodyExtractor;
        this.htmlImageExtractor = htmlImageExtractor;
    }

    public String extractImageFrom(SyndEntry item) {
        final MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) item.getModule(MediaModule.URI);
        if (mediaModule != null) {
            log.debug("Media module found for item: " + item.getTitle());

            final MediaContent[] mediaContents = mediaModule.getMediaContents();
            MediaContent selectedMediaContent = null;
            for (int i = 0; i < mediaContents.length; i++) {
                MediaContent mediaContent = mediaContents[i];
                final boolean isImage = isImage(mediaContent);
                if (isImage && !isBlockListed(mediaContent) && isBetterThanCurrentlySelected(mediaContent, selectedMediaContent)) {
                    selectedMediaContent = mediaContent;
                }
            }

            if (selectedMediaContent != null) {
                log.debug("Took image reference from MediaContent: " + selectedMediaContent.getReference().toString());
                return selectedMediaContent.getReference().toString();
            }

        }

        // Look for img srcs in html content
        final String itemBody = rssFeedItemBodyExtractor.extractBody(item);
        if (!Strings.isNullOrEmpty(itemBody)) {
            String imageSrc = htmlImageExtractor.extractImage(itemBody);
            if (!Strings.isNullOrEmpty(imageSrc)) {
                imageSrc = ensureFullyQualifiedUrl(imageSrc, item.getLink());
                if (imageSrc != null && !isBlockListedImageUrl(imageSrc)) {
                    return imageSrc;
                }
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

    private boolean isBlockListed(MediaContent mediaContent) {
        if (mediaContent.getReference() != null && mediaContent.getReference().toString() != null) {
            return isBlockListedImageUrl(mediaContent.getReference().toString());
        }
        return false;
    }

    private boolean isBlockListedImageUrl(String url) {
        return blockedUrlSnippets.stream().anyMatch(url::contains);
    }

    private boolean isImage(MediaContent mediaContent) {
        final boolean hasTypeJpegAttribute = mediaContent.getType() != null && mediaContent.getType().equals("image/jpeg");
        final boolean isJpegUrl = mediaContent.getReference() != null && mediaContent.getReference().toString().contains(".jpg");
        // TODO Wordpress looks to use medium="image"
        return mediaContent.getReference() != null && (hasTypeJpegAttribute || isJpegUrl);
    }

    private boolean isBetterThanCurrentlySelected(MediaContent mediaContent, MediaContent selectedMediaContent) {
        if (selectedMediaContent == null) {
            return true;
        }
        return mediaContent.getWidth() != null && mediaContent.getWidth() > selectedMediaContent.getWidth();
    }

}
