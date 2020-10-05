package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MediaModuleImageExtractor {

    private final static Logger log = Logger.getLogger(MediaModuleImageExtractor.class);

    private final Set<String> blockedUrlSnippets = Set.of("http://stats.wordpress.com", "gravatar.com/avatar", "share_save_171_16");

    public String extractImageFromMediaModule(SyndEntry item) {
        final MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) item.getModule(MediaModule.URI);
        if (mediaModule != null) {
            log.debug("Media module found for item: " + item.getTitle());

            final Stream<MediaContent> mediaContents = Arrays.stream(mediaModule.getMediaContents());

            Comparator<MediaContent> latestImageFirst = Comparator.comparing(MediaContent::getWidth);
            Stream<MediaContent> nonBlockListedImages = mediaContents.
                    filter(this::isImage).
                    filter(image -> !isBlockListed(image)).
                    sorted(latestImageFirst);   // prefer the latest available image TODO test coverage required

            Optional<MediaContent> first = nonBlockListedImages.findFirst();
            if (first.isPresent()) {
                String choosenImage = first.get().getReference().toString();
                log.debug("Took image reference from MediaContent: " + choosenImage);
                return choosenImage;
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

    private boolean isBlockListed(MediaContent mediaContent) {
        return blockedUrlSnippets.stream().anyMatch(mediaContent.getReference().toString()::contains);
    }

}
