package uk.co.eelpieconsulting.feedlistener.rss

import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.io.FeedException
import io.micrometer.core.instrument.MeterRegistry
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.http.HttpFetchException
import uk.co.eelpieconsulting.common.http.HttpFetcher
import uk.co.eelpieconsulting.feedlistener.model.FeedItem

@Component
class FeedFetcher @Autowired constructor(private val httpFetcher: HttpFetcher,
                                         private val feedParser: FeedParser,
                                         private val rssFeedItemMapper: RssFeedItemMapper,
                                         meterRegistry: MeterRegistry) {

    private val log = Logger.getLogger(FeedFetcher::class.java)

    private val rssFetchesCounter = meterRegistry.counter("rss_fetches")
    private val rssFetchedBytesCounter = meterRegistry.counter("rss_fetched_bytes")

    @Throws(HttpFetchException::class, FeedException::class)
    fun fetchFeed(url: String): FetchedFeed? {
        val syndfeed = loadSyndFeedWithFeedFetcher(url)
        return FetchedFeed(feedName = syndfeed.title, feedItems = getFeedItemsFrom(syndfeed)) // TODO we'd like to be able to capture etag and other caching related headers
    }

    @Throws(HttpFetchException::class, FeedException::class)
    private fun loadSyndFeedWithFeedFetcher(feedUrl: String): SyndFeed {
        log.info("Loading SyndFeed from url: " + feedUrl)
        rssFetchesCounter.increment()
        val fetchedBytes = httpFetcher.getBytes(feedUrl)
        rssFetchedBytesCounter.increment(fetchedBytes.size.toDouble())
        return feedParser.parseSyndFeed(fetchedBytes)
    }

    private fun getFeedItemsFrom(syndfeed: SyndFeed): List<FeedItem> {
        return syndfeed.entries.iterator().asSequence().map { entry ->
            if (entry is SyndEntry) rssFeedItemMapper.createFeedItemFrom(entry) else null
        }.filterNotNull().toList()
    }
}