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

    @Test
    fun canMapRssSyndEntriesToFeedItem() {
        val rssFeedItemImageExtractor = RssFeedItemImageExtractor(
                BodyHtmlImageExtractor(RssFeedItemBodyExtractor()),
                MediaModuleImageExtractor()
        )
        val rssFeedItemBodyExtractor = RssFeedItemBodyExtractor()
        val cachingUrlResolverService = mock(CachingUrlResolverService::class.java)
        `when`(cachingUrlResolverService.resolveUrl(anyObject())).thenReturn("http://localhost/something")
        val rssFeedItemMapper = RssFeedItemMapper(
                rssFeedItemImageExtractor,
                rssFeedItemBodyExtractor,
                cachingUrlResolverService,
                UrlCleaner()
        )

        val input = IOUtils.toString(FileInputStream(this.javaClass.classLoader.getResource("inside-wellington-media-break.xml").file))
        val result = FeedParser().parseSyndFeed(input.toByteArray())
        val syndFeed = result.get()

        val subscription = RssSubscription(url = "123", channelId = "123", username = "123")
        val feedItems = syndFeed.entries.iterator().asSequence().map { entire ->
            rssFeedItemMapper.createFeedItemFrom((entire as SyndEntry), subscription)
        }.toList()

        assertEquals(10, feedItems.size)
    }

}