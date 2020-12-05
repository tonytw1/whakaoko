package uk.co.eelpieconsulting.feedlistener.rss

import com.sun.syndication.feed.synd.SyndEntry
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.rss.images.BodyHtmlImageExtractor
import uk.co.eelpieconsulting.feedlistener.rss.images.MediaModuleImageExtractor
import uk.co.eelpieconsulting.feedlistener.rss.images.RssFeedItemImageExtractor
import java.io.FileInputStream
import org.mockito.Mockito.mock
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

class RssFeedItemMapperTest {

    @Test
    fun insideWellingtonMediaBreak() {
        val rssFeedItemImageExtractor = RssFeedItemImageExtractor(
                BodyHtmlImageExtractor(RssFeedItemBodyExtractor()),
                MediaModuleImageExtractor()
        )
        val rssFeedItemBodyExtractor = RssFeedItemBodyExtractor()
        val cachingUrlResolverService = mock(CachingUrlResolverService::class.java)

        val rssFeedItemMapper = RssFeedItemMapper(
                rssFeedItemImageExtractor,
                rssFeedItemBodyExtractor,
                cachingUrlResolverService,
                UrlCleaner()
        )

        val input = IOUtils.toString(FileInputStream(this.javaClass.classLoader.getResource("inside-wellington-media-break.xml").file))
        val result = FeedParser().parseSyndFeed(input.toByteArray())
        val syndFeed = result.get()

        val subscription = RssSubscription()
        syndFeed.entries.iterator().asSequence().map { entire ->
            rssFeedItemMapper.createFeedItemFrom((entire as SyndEntry), subscription)
        }.toList()
    }

}