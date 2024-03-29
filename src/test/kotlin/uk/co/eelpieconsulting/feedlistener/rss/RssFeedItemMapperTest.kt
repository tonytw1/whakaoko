package uk.co.eelpieconsulting.feedlistener.rss

import com.rometools.rome.feed.synd.SyndEntry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.rss.images.BodyHtmlImageExtractor
import uk.co.eelpieconsulting.feedlistener.rss.images.MediaModuleImageExtractor
import uk.co.eelpieconsulting.feedlistener.rss.images.RssFeedItemImageExtractor
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class RssFeedItemMapperTest {

    private val meterRegistry = SimpleMeterRegistry()

    private val rssFeedItemImageExtractor = RssFeedItemImageExtractor(
            BodyHtmlImageExtractor(RssFeedItemBodyExtractor()),
            MediaModuleImageExtractor(),
        meterRegistry
    )
    private val rssFeedItemBodyExtractor = RssFeedItemBodyExtractor()
    private val urlResolverService = mock(UrlResolverService::class.java)

    private val rssFeedItemMapper = RssFeedItemMapper(
            rssFeedItemImageExtractor,
            rssFeedItemBodyExtractor,
            urlResolverService,
            UrlCleaner()
    )

    private val subscription = RssSubscription(url = "a-subscription", channelId = "a-channel", username = "a-user")


    @Test
    fun canMapRssSyndEntriesToFeedItem() {
        `when`(urlResolverService.resolveUrl(any())).thenReturn("http://localhost/something")

        val feedItems = testSyndEntries().map { rssFeedItemMapper.createFeedItemFrom(it, subscription) }

        assertEquals(10, feedItems.size)
    }

    @Test
    fun feedItemsShouldInheritSubscriptionAndChannel() {
        `when`(urlResolverService.resolveUrl(any())).thenReturn("http://localhost/something")

        val feedItems = testSyndEntries().map { rssFeedItemMapper.createFeedItemFrom(it, subscription) }

        val feedItem = feedItems.first()
        assertEquals(subscription.channelId, feedItem?.channelId)
        assertEquals(subscription.id, feedItem?.subscriptionId)
    }

    @Test
    fun shouldStripFigureTags() {
        `when`(urlResolverService.resolveUrl(any())).thenReturn("http://localhost/something")

        val feedItems = testSyndEntries("blackcoffee-figures.xml").map { rssFeedItemMapper.createFeedItemFrom(it, subscription) }

        val feedItem = feedItems.first()
        assertTrue(feedItem?.body!!.startsWith("WHAKATAU MAI RĀ E NGĀ HAU E WHĀ Calling In The Four Winds I have been contemplating my life over the last few years,"))
        assertFalse(feedItem?.body!!.contains("<figure>"))
    }

    @Test
    fun canExtractCategoriesFromRssItems() {
        `when`(urlResolverService.resolveUrl(any())).thenReturn("http://localhost/something")

        val feedItems = testSyndEntries("toiponeke.xml").map { rssFeedItemMapper.createFeedItemFrom(it, subscription) }
        val first = feedItems.first()
        assertEquals(1, first?._categories?.size)
        assertEquals("Emerging Production Designer Residency 2022 - Call for applications", first?.headline)
        assertEquals("residency", first?._categories?.first()?.value)

        val cdataEncodedFeedItems = testSyndEntries("nra-feed.xml").map { rssFeedItemMapper.createFeedItemFrom(it, subscription) }
        val firstCdataEncoded = cdataEncodedFeedItems.first()
        assertEquals("Draft District Plan, Bike Network and LGWM – 3 consultations open now!", firstCdataEncoded?.title)
        assertEquals(listOf("Consultation", "News"), firstCdataEncoded?._categories?.map{it.value})
    }

    private fun testSyndEntries(filename: String = "inside-wellington-media-break.xml"): List<SyndEntry> {
        val input = IOUtils.toString(FileInputStream(this.javaClass.classLoader.getResource(filename)!!.file), StandardCharsets.UTF_8)
        val result = FeedParser().parseSyndFeed(input.toByteArray())
        return result.get().entries.asSequence().map { entry -> entry as SyndEntry }.toList()
    }

}