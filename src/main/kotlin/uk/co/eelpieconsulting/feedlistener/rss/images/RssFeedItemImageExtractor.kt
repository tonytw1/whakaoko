package uk.co.eelpieconsulting.feedlistener.rss.images

import com.rometools.rome.feed.synd.SyndEntry
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.MalformedURLException
import java.net.URL

@Component
class RssFeedItemImageExtractor @Autowired constructor(
    bodyHtmlImageExtractor: BodyHtmlImageExtractor,
    mediaModuleImageExtractor: MediaModuleImageExtractor,
    meterRegistry: MeterRegistry
) {
    private val blockedUrlSnippets = setOf("http://stats.wordpress.com", "gravatar.com/avatar", "share_save_171_16")
    private val bodyHtmlImageExtractor: BodyHtmlImageExtractor
    private val mediaModuleImageExtractor: MediaModuleImageExtractor

    private val rssImage: Counter
    private val rssImageMediaModule: Counter
    private val rssImageBodyHtml: Counter

    private val log = LogManager.getLogger(RssFeedItemImageExtractor::class.java)

    init {
        this.bodyHtmlImageExtractor = bodyHtmlImageExtractor
        this.mediaModuleImageExtractor = mediaModuleImageExtractor
        rssImage = meterRegistry.counter("rss_image")
        rssImageMediaModule = meterRegistry.counter("rss_image_media_module")
        rssImageBodyHtml = meterRegistry.counter("rss_image_body_html")
    }

    fun extractImageFrom(item: SyndEntry): String? {
        rssImage.increment()

        // Look for an RSS media module image; if that fails try to extract an image from the HTML body.
        val mediaModuleImage = mediaModuleImageExtractor.extractImageFromMediaModule(item)
        if (mediaModuleImage != null) {
            rssImageMediaModule.increment()
            return mediaModuleImage
        }
        val bodyHtmlImage = bodyHtmlImageExtractor.extractImageFrom(item)
        if (bodyHtmlImage != null) {
            // Attempt to expand relative image paths to fully qualified urls.
            val fullyQualifiedImagePath = ensureFullyQualifiedUrl(bodyHtmlImage, item.link)
            if (fullyQualifiedImagePath != null && !isBlockListedImageUrl(fullyQualifiedImagePath)) { // TODO specific to HTML only?
                rssImageBodyHtml.increment()
                return fullyQualifiedImagePath
            }
        }
        log.debug("No suitable media element image seen")
        return null
    }

    private fun ensureFullyQualifiedUrl(imageSrc: String, itemUrl: String?): String? {
        return if (imageSrc.startsWith("/") && itemUrl != null) {
            log.debug("Image src looks like a relative url; attempting to fully qualify")
            try {
                val itemUrlUrl = URL(itemUrl)
                val fullyQualifiedImageSrc: String = itemUrlUrl.protocol + "://" + itemUrlUrl.host + imageSrc
                val fullyQualifiedImageSrcUrl = URL(fullyQualifiedImageSrc)
                log.debug("Referenced from root image src resolved to fully qualified url: " + fullyQualifiedImageSrcUrl.toExternalForm())
                fullyQualifiedImageSrcUrl.toExternalForm()
            } catch (e: MalformedURLException) {
                log.warn("Item base url or generated referenced from root image src url is not well formed; returning null")
                null
            }
        } else {
            try {
                val imageSrcUrl = URL(imageSrc)
                imageSrcUrl.toExternalForm()
            } catch (e: MalformedURLException) {
                log.debug("Returning null fully qualified image url for: $imageSrc / $itemUrl")
                null
            }
        }
    }

    private fun isBlockListedImageUrl(url: String): Boolean {
        return blockedUrlSnippets.stream().anyMatch(url::contains)
    }

}
