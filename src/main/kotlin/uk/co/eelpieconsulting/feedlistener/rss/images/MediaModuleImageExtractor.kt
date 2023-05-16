package uk.co.eelpieconsulting.feedlistener.rss.images

import com.rometools.modules.mediarss.MediaEntryModuleImpl
import com.rometools.modules.mediarss.MediaModule
import com.rometools.modules.mediarss.types.MediaContent
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Stream

@Component
class MediaModuleImageExtractor {

    private val log = LogManager.getLogger(MediaModuleImageExtractor::class.java)

    private val blockedUrlSnippets = setOf("http://stats.wordpress.com", "gravatar.com/avatar", "share_save_171_16") // TODO push up

    private val latestImageFirstCompator: Comparator<MediaContent> = Comparator.comparing { mediaContent -> if (mediaContent.getWidth() != null) mediaContent.getWidth() else 0 }

    fun extractImageFromMediaModule(item: SyndEntry): String? {
        if (item.getModule(MediaModule.URI) == null) {
            log.debug("No media module found for item: " + item.getTitle())
            return null
        }
        val mediaModule: MediaEntryModuleImpl = item.getModule(MediaModule.URI) as MediaEntryModuleImpl
        return if (mediaModule != null) {
            log.debug("Media module found for item: " + item.getTitle())
            val mediaContents = Arrays.stream(mediaModule.getMediaContents())

            val nonBlockListedImages: Stream<MediaContent?> =
                mediaContents.filter { mediaContent: MediaContent -> isImage(mediaContent) }
                    .filter { image -> !isBlockListed(image) }
                    .sorted(latestImageFirstCompator) // prefer the latest available image TODO test coverage required

            val maybeFirst = nonBlockListedImages.findFirst()
            if (maybeFirst.isPresent()) {
                val choosenImage = maybeFirst.get().getReference().toString()
                log.debug("Took image reference from MediaContent: $choosenImage")
                choosenImage

            } else {
                null
            }
        } else {
            null
        }
    }

    private fun isImage(mediaContent: MediaContent): Boolean {
        val hasTypeJpegAttribute = mediaContent.getType() != null && mediaContent.getType().equals("image/jpeg")
        val isJpegUrl = mediaContent.getReference() != null && mediaContent.getReference().toString().contains(".jpg")
        // TODO Wordpress looks to use medium="image"
        return mediaContent.getReference() != null && (hasTypeJpegAttribute || isJpegUrl)
    }

    private fun isBlockListed(mediaContent: MediaContent): Boolean {
        return blockedUrlSnippets.stream().anyMatch(mediaContent.getReference().toString()::contains)
    }

}
