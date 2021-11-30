package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import com.sun.syndication.feed.module.georss.GeoRSSUtils
import com.sun.syndication.feed.synd.SyndCategory
import com.sun.syndication.feed.synd.SyndEntry
import org.apache.commons.lang.StringEscapeUtils
import org.apache.logging.log4j.LogManager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.html.HtmlCleaner
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.LatLong
import uk.co.eelpieconsulting.feedlistener.model.Place
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import uk.co.eelpieconsulting.feedlistener.rss.images.RssFeedItemImageExtractor

@Component
class RssFeedItemMapper @Autowired constructor(private val rssFeedItemImageExtractor: RssFeedItemImageExtractor,
                                               private val rssFeedItemBodyExtractor: RssFeedItemBodyExtractor,
                                               private val urlResolverService: UrlResolverService,
                                               private val urlCleaner: UrlCleaner) {

    private val log = LogManager.getLogger(RssFeedItemMapper::class.java)

    fun createFeedItemFrom(syndEntry: SyndEntry, subscription: Subscription): FeedItem? {
        val place = extractLocationFrom(syndEntry)
        val imageUrl = rssFeedItemImageExtractor.extractImageFrom(syndEntry)
        val body =
            HtmlCleaner().stripHtml(StringEscapeUtils.unescapeHtml(rssFeedItemBodyExtractor.extractBody(syndEntry)))
        val date = if (syndEntry.publishedDate != null) syndEntry.publishedDate else syndEntry.updatedDate
        val url = extractUrl(syndEntry)

        val categories: List<String> = syndEntry.categories.mapNotNull {
            if (it is SyndCategory) {
                it.name // TODO capture domain as well
            } else {
                null
            }
        }

        if (url != null && date != null) {
            return FeedItem(
                syndEntry.title,
                url,
                body,
                date,
                place,
                imageUrl,
                syndEntry.author,
                subscription.id,
                subscription.channelId,
                categories
            )
        } else {
            log.warn("Saw syndEntry with no url or date: $syndEntry")
            return null
        }
    }

    private fun extractUrl(syndEntry: SyndEntry): String? {
        val url = syndEntry.link
        return if (!Strings.isNullOrEmpty(url)) {
            urlCleaner.cleanSubmittedItemUrl(urlResolverService.resolveUrl(url))
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