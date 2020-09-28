package uk.co.eelpieconsulting.feedlistener.rss

import com.github.kittinunf.result.Result
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import io.micrometer.core.instrument.MeterRegistry
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.http.HttpFetcher
import uk.co.eelpieconsulting.feedlistener.http.HttpResult
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.Subscription

@Component
class FeedFetcher @Autowired constructor(private val httpFetcher: HttpFetcher,
                                         private val feedParser: FeedParser,
                                         private val rssFeedItemMapper: RssFeedItemMapper,
                                         meterRegistry: MeterRegistry) {

    private val log = Logger.getLogger(FeedFetcher::class.java)

    private val rssFetchesCounter = meterRegistry.counter("rss_fetches")
    private val rssFetchedBytesCounter = meterRegistry.counter("rss_fetched_bytes")

    fun fetchFeed(subscription: RssSubscription): Result<FetchedFeed, FeedFetchingException> {
        loadSyndFeedWithFeedFetcher(subscription.url).fold({ syndFeedAndHttpResult ->
            val syndFeed = syndFeedAndHttpResult.first
            val httpResult = syndFeedAndHttpResult.second

            val headers = httpResult.headers
            val etag = headers["ETag"].firstOrNull()

            val fetchedFeed = FetchedFeed(feedName = syndFeed.title, feedItems = getFeedItemsFrom(syndFeed, subscription), etag = etag, httpStatus = httpResult.status)
            return Result.success(fetchedFeed)

        }, { ex ->
            return Result.Failure(ex)
        })
    }

    private fun loadSyndFeedWithFeedFetcher(feedUrl: String): Result<Pair<SyndFeed, HttpResult>, FeedFetchingException> {
        log.info("Loading SyndFeed from url: " + feedUrl)
        rssFetchesCounter.increment()

        httpFetcher.getBytes(feedUrl).fold({ httpResult ->
            val fetchedBytes = httpResult.bytes
            rssFetchedBytesCounter.increment(fetchedBytes.size.toDouble())

            try {
                val syndFeed = feedParser.parseSyndFeed(fetchedBytes)
                return Result.Success(Pair(syndFeed, httpResult))

            } catch (ex: Exception) {
                log.warn("Feed parsing error: " + ex.message)
                return Result.error(FeedFetchingException(message = ex.message!!, httpStatus = httpResult.status))
            }
        }, { fuelError ->
            return Result.Failure(FeedFetchingException(message = fuelError.message!!, fuelError.response.statusCode))
        })
    }

    private fun getFeedItemsFrom(syndfeed: SyndFeed, subscription: Subscription): List<FeedItem> {
        return syndfeed.entries.iterator().asSequence().map { entry ->
            if (entry is SyndEntry) rssFeedItemMapper.createFeedItemFrom(entry, subscription) else null
        }.filterNotNull().toList()
    }
}

class FeedFetchingException(message: String, val httpStatus: Int? = null): Exception(message)