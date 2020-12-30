package uk.co.eelpieconsulting.feedlistener.rss

import com.sun.syndication.feed.synd.SyndEntry
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyObject
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.rss.images.BodyHtmlImageExtractor
import uk.co.eelpieconsulting.feedlistener.rss.images.MediaModuleImageExtractor
import uk.co.eelpieconsulting.feedlistener.rss.images.RssFeedItemImageExtractor
import java.io.FileInputStream

class RssFeedItemMapperTest {

    private val rssFeedItemImageExtractor = RssFeedItemImageExtractor(
            BodyHtmlImageExtractor(RssFeedItemBodyExtractor()),
            MediaModuleImageExtractor()
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
        `when`(urlResolverService.resolveUrl(anyObject())).thenReturn("http://localhost/something")

        val feedItems = testSyndEntries().map { rssFeedItemMapper.createFeedItemFrom(it, subscription) }

        assertEquals(10, feedItems.size)
    }

    @Test
    fun feedItemsShouldInheritSubscriptionAndChannel() {
        `when`(urlResolverService.resolveUrl(anyObject())).thenReturn("http://localhost/something")

        val feedItems = testSyndEntries().map { rssFeedItemMapper.createFeedItemFrom(it, subscription) }

        val feedItem = feedItems.first()
        assertEquals(subscription.channelId, feedItem?.channelId)
        assertEquals(subscription.id, feedItem?.subscriptionId)
    }

    private fun testSyndEntries(): List<SyndEntry> {
        val input = IOUtils.toString(FileInputStream(this.javaClass.classLoader.getResource("inside-wellington-media-break.xml").file))
        val result = FeedParser().parseSyndFeed(input.toByteArray())
        return result.get().entries.asSequence().map { entry -> entry as SyndEntry }.toList()
    }

}