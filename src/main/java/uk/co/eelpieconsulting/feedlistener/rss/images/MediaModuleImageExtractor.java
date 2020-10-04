package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.log4j.Logger;

import java.util.Set;

public class MediaModuleImageExtractor {

    private final static Logger log = Logger.getLogger(MediaModuleImageExtractor.class);
    private final Set<String> blockedUrlSnippets = Set.of("http://stats.wordpress.com", "gravatar.com/avatar", "share_save_171_16");

    public String extractImageFromMediaModule(SyndEntry item) {
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
        return null;
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

    private boolean isBlockListed(MediaContent mediaContent) {
        if (mediaContent.getReference() != null && mediaContent.getReference().toString() != null) {
            return isBlockListedImageUrl(mediaContent.getReference().toString());
        }
        return false;
    }
    private boolean isBlockListedImageUrl(String url) { // TODO duplication
        return blockedUrlSnippets.stream().anyMatch(url::contains);
    }

}
