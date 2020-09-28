package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import com.sun.syndication.feed.module.georss.GeoRSSUtils
import com.sun.syndication.feed.synd.SyndEntry
import org.apache.commons.lang.StringEscapeUtils
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.html.HtmlCleaner
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.LatLong
import uk.co.eelpieconsulting.feedlistener.model.Place
import uk.co.eelpieconsulting.feedlistener.model.Subscription

@Component
class RssFeedItemMapper @Autowired constructor(private val rssFeedItemImageExtractor: RssFeedItemImageExtractor,
                                               private val rssFeedItemBodyExtractor: RssFeedItemBodyExtractor,
                                               private val cachingUrlResolverService: CachingUrlResolverService,
                                               private val urlCleaner: UrlCleaner) {

    private val log = Logger.getLogger(RssFeedItemMapper::class.java)

    fun createFeedItemFrom(syndEntry: SyndEntry, subscription: Subscription): FeedItem? {
        val place = extractLocationFrom(syndEntry)
        val imageUrl = rssFeedItemImageExtractor.extractImageFrom(syndEntry)
        val body = HtmlCleaner().stripHtml(StringEscapeUtils.unescapeHtml(rssFeedItemBodyExtractor.extractBody(syndEntry)))
        val date = if (syndEntry.publishedDate != null) syndEntry.publishedDate else syndEntry.updatedDate
        val url = extractUrl(syndEntry)
        if (url != null) {
            return FeedItem(syndEntry.title, url, body, date, place, imageUrl, null, subscription.id, subscription.channelId)
        } else {
            return null
        }
    }

    private fun extractUrl(syndEntry: SyndEntry): String? {
        val url = syndEntry.link
        return if (!Strings.isNullOrEmpty(url)) {
            urlCleaner.cleanSubmittedItemUrl(cachingUrlResolverService.resolveUrl(url))
        } else {
            null
        }
    }

    private fun extractLocationFrom(syndEntry: SyndEntry): Place? {
        val geoModule = GeoRSSUtils.getGeoRSS(syndEntry)
        if (geoModule != null && geoModule.position != null) {
            val latLong = LatLong(geoModule.position.latitude, geoModule.position.longitude)
            log.debug("Rss item '" + syndEntry.title + "' has position information: " + latLong)
            return Place(null, latLong)
        }
        return null
    }

}