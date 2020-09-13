package uk.co.eelpieconsulting.feedlistener.rss

import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.io.FeedException
import io.micrometer.core.instrument.MeterRegistry
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.http.HttpFetchException
import uk.co.eelpieconsulting.feedlistener.http.HttpFetcher
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import com.github.kittinunf.result.Result

@Component
class FeedFetcher @Autowired constructor(private val httpFetcher: HttpFetcher,
                                         private val feedParser: FeedParser,
                                         private val rssFeedItemMapper: RssFeedItemMapper,
                                         meterRegistry: MeterRegistry) {

    private val log = Logger.getLogger(FeedFetcher::class.java)

    private val rssFetchesCounter = meterRegistry.counter("rss_fetches")
    private val rssFetchedBytesCounter = meterRegistry.counter("rss_fetched_bytes")

    @Throws(HttpFetchException::class, FeedException::class)
    fun fetchFeed(url: String): FetchedFeed {
        val result = loadSyndFeedWithFeedFetcher(url)
        when (result) {
            is Result.Success -> {
                val syndFeed = result.value.first
                val fetchedFeed = FetchedFeed(feedName = syndFeed.title, feedItems = getFeedItemsFrom(syndFeed), etag = result.value.second)
                return fetchedFeed
            }
            is Result.Failure -> {
                throw(result.error)
            }
        }
    }

    private fun loadSyndFeedWithFeedFetcher(feedUrl: String): Result<Pair<SyndFeed, String?>, Exception> {
        log.info("Loading SyndFeed from url: " + feedUrl)
        rssFetchesCounter.increment()
        val result = httpFetcher.getBytes(feedUrl)
        when (result) {
            is Result.Success -> {
                val fetchedBytes = result.value.first
                rssFetchedBytesCounter.increment(fetchedBytes.size.toDouble())
                return Result.Success(Pair(feedParser.parseSyndFeed(fetchedBytes), result.value.second))
            }
            is Result.Failure -> {
                return Result.Failure(result.error)
            }
        }
    }

    private fun getFeedItemsFrom(syndfeed: SyndFeed): List<FeedItem> {
        return syndfeed.entries.iterator().asSequence().map { entry ->
            if (entry is SyndEntry) rssFeedItemMapper.createFeedItemFrom(entry) else null
        }.filterNotNull().toList()
    }
}