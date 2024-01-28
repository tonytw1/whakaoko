package uk.co.eelpieconsulting.feedlistener.rss.images

import com.rometools.modules.mediarss.MediaModule
import com.rometools.rome.feed.synd.SyndEntry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class RssFeedItemImageExtractorTest {
    private val bodyHtmlImageExtractor = mock(BodyHtmlImageExtractor::class.java)
    private val meterRegistry = SimpleMeterRegistry()
    private val extractor = RssFeedItemImageExtractor(
        bodyHtmlImageExtractor,
        MediaModuleImageExtractor(),
        meterRegistry
    )

    private val itemWithImageInHtmlBody = mock(SyndEntry::class.java)

    private val fullyQualifiedImageUrl = "http://www.localhost/images/test.jpg"
    private val imagePath = "/images/test.jpg"

    @Test
    fun shouldExtractFirstImageFromHtmlItemBodyIfAvailable() {
        `when`(itemWithImageInHtmlBody.getModule(MediaModule.URI)).thenReturn(null)
        `when`(bodyHtmlImageExtractor.extractImageFrom(itemWithImageInHtmlBody)).thenReturn(fullyQualifiedImageUrl)

        val imageUrl = extractor.extractImageFrom(itemWithImageInHtmlBody)

        assertEquals(fullyQualifiedImageUrl, imageUrl)
    }

    @Test
    fun shouldExtendReferencedFromTheRootImagesFoundInHtmlIntoFullyQualifiedUrlsBasedOnTheItemUrl() {
        `when`(itemWithImageInHtmlBody.link).thenReturn("http://www.localhost/posts/123")
        `when`(itemWithImageInHtmlBody.getModule(MediaModule.URI)).thenReturn(null)
        `when`(bodyHtmlImageExtractor.extractImageFrom(itemWithImageInHtmlBody)).thenReturn(imagePath)

        val imageUrl = extractor.extractImageFrom(itemWithImageInHtmlBody)

        assertEquals(fullyQualifiedImageUrl, imageUrl)
    }

}
