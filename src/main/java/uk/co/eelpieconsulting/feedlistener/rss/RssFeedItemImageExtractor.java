package uk.co.eelpieconsulting.feedlistener.rss;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndEntry;

@Component
public class RssFeedItemImageExtractor {

    private static Logger log = Logger.getLogger(RssFeedItemImageExtractor.class);

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
                if (isImage && !isBlackListed(mediaContent) && isBetterThanCurrentlySelected(mediaContent, selectedMediaContent)) {
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
                if (imageSrc != null && !isBlackListedImageUrl(imageSrc)) {
                    return imageSrc;
                }
            }
        }

        log.debug("No suitable media element image seen");
        return null;
    }

    private String ensureFullyQualifiedUrl(final String imageSrc, String itemUrl) {
        try {
            URL imageSrcUrl = new URL(imageSrc);
            return imageSrcUrl.toExternalForm();

        } catch (MalformedURLException e) {
        }

        log.debug("Image src is not a url; attempting to fully qualify");
        if (imageSrc.startsWith("/")) {
            try {
                final URL itemUrlUrl = new URL(itemUrl);
                final String fullyQualifiedImageSrc = itemUrlUrl.getProtocol() + "://" + itemUrlUrl.getHost() + imageSrc;

                final URL fullyQualifiedImageSrcUrl = new URL(fullyQualifiedImageSrc);
                log.debug("Referenced from root image src resolved to fully qualified url: " + fullyQualifiedImageSrcUrl.toExternalForm());
                return fullyQualifiedImageSrcUrl.toExternalForm();

            } catch (MalformedURLException e) {
                log.warn("Item base url or generated referenced from root image src url is not well formed");
            }
        }

        log.debug("Returning null fully qualified image url for: " + imageSrc + " / " + itemUrl);
        return null;
    }

    private boolean isBlackListed(MediaContent mediaContent) {
        if (mediaContent.getReference() != null && mediaContent.getReference().toString() != null) {
            return isBlackListedImageUrl(mediaContent.getReference().toString());
        }
        return false;
    }

    private boolean isBlackListedImageUrl(String url) {
        return url.startsWith("http://stats.wordpress.com") && !url.contains("gravatar.com/avatar") && !url.contains("share_save_171_16");
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
