package uk.co.eelpieconsulting.feedlistener.rss

import com.github.kittinunf.result.Result
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import io.micrometer.core.instrument.MeterRegistry
import org.apache.logging.log4j.LogManager

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

    private val log = LogManager.getLogger(FeedFetcher::class.java)

    private val rssFetchesCounter = meterRegistry.counter("rss_fetches")
    private val rssFetchedBytesCounter = meterRegistry.counter("rss_fetched_bytes")

    fun fetchFeed(subscription: RssSubscription): Result<Pair<FetchedFeed?, HttpResult>, FeedFetchingException> {
        loadSyndFeedWithFeedFetcher(subscription.url, subscription.etag).fold({ syndFeedAndHttpResult ->
            val maybeSyndFeed = syndFeedAndHttpResult.first
            val maybeFetchedFeed = maybeSyndFeed?.let { syndFeed ->
                val httpResult = syndFeedAndHttpResult.second
                val headers = httpResult.headers
                val etag = headers["ETag"].firstOrNull()
                FetchedFeed(feedName = maybeSyndFeed.title, feedItems = getFeedItemsFrom(maybeSyndFeed, subscription), etag = etag, httpStatus = httpResult.status)
            }
            return Result.Success(Pair(maybeFetchedFeed, syndFeedAndHttpResult.second))
        }, { ex ->
            return Result.Failure(ex)
        })
    }

    private fun loadSyndFeedWithFeedFetcher(feedUrl: String, etag: String?): Result<Pair<SyndFeed?, HttpResult>, FeedFetchingException> {
        log.info("Loading SyndFeed from url: " + feedUrl)
        rssFetchesCounter.increment()

        httpFetcher.getBytes(feedUrl, etag).fold({ httpResult ->
            // Always increment the bytes counter even for non 200 requests
            // The total amount of traffic we are generating is an important metric
            val fetchedBytes = httpResult.bytes
            rssFetchedBytesCounter.increment(fetchedBytes.size.toDouble())

            if (httpResult.status == 200) {
                return feedParser.parseSyndFeed(fetchedBytes).fold({ syndFeed ->
                    Result.success(Pair(syndFeed, httpResult))
                }, { ex ->
                    log.warn("Feed parsing error: " + ex.message)
                    Result.error(
                        FeedFetchingException(
                            message = ex.message!!,
                            httpStatus = httpResult.status,
                            rootCause = ex
                        )
                    )
                })

            }  else if (httpResult.status == 304) {
                // Not modified
                log.info("Feed url responded with 304 not modified: " + feedUrl)
                return Result.success(Pair(null, httpResult))

            } else {
                return Result.Failure(FeedFetchingException(message = "Could not fetch feed", httpResult.status))
            }

        }, { fuelError ->
            return Result.Failure(FeedFetchingException(message = fuelError.message!!, fuelError.response.statusCode, fuelError))
        })
    }

    private fun getFeedItemsFrom(syndfeed: SyndFeed, subscription: Subscription): List<FeedItem> {
        return syndfeed.entries.iterator().asSequence().map { entry ->
            if (entry is SyndEntry) rssFeedItemMapper.createFeedItemFrom(entry, subscription) else null
        }.filterNotNull().toList()
    }
}

class FeedFetchingException(message: String, val httpStatus: Int? = null, val rootCause: Throwable? = null): Exception(message)