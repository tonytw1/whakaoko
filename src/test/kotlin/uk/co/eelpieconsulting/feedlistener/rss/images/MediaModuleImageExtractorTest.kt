package uk.co.eelpieconsulting.feedlistener.rss.images

import com.rometools.modules.mediarss.MediaEntryModuleImpl
import com.rometools.modules.mediarss.MediaModule
import com.rometools.modules.mediarss.types.MediaContent
import com.rometools.modules.mediarss.types.UrlReference
import com.rometools.rome.feed.synd.SyndEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class MediaModuleImageExtractorTest {
    private val itemWithImage = mock(SyndEntry::class.java)
    private val mediaModuleWithImage = mock(MediaEntryModuleImpl::class.java)
    private val extractor = MediaModuleImageExtractor()

    private val FULLY_QUALIFIED_IMAGE_URL: String = "http://www.localhost/images/test.jpg"
    @Test
    fun shouldExtractUrlOfFullyQualifiedMediaRssImageElements() {
        val image = MediaContent(UrlReference(FULLY_QUALIFIED_IMAGE_URL))
        val mediaContents = arrayOf<MediaContent?>(image)
        `when`(itemWithImage.getModule(MediaModule.URI)).thenReturn(mediaModuleWithImage)
        `when`(mediaModuleWithImage.mediaContents).thenReturn(mediaContents)

        val imageUrl = extractor.extractImageFromMediaModule(itemWithImage)

        assertEquals(FULLY_QUALIFIED_IMAGE_URL, imageUrl)
    }

}
