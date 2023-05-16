package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import com.rometools.modules.georss.GeoRSSUtils
import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.lang.StringEscapeUtils
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.html.HtmlCleaner
import uk.co.eelpieconsulting.feedlistener.model.*
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

        val categories: List<Category> = syndEntry.categories.mapNotNull {
            if (it is SyndCategory) {
                Category(it.name)
            } else {
                null
            }
        }

        val accepted = DateTime.now().toDate()
        val ordering = date ?: accepted

        if (url != null) {
            return FeedItem(
                syndEntry.title,
                url,
                body,
                date,
                accepted,
                place,
                imageUrl,
                syndEntry.author,
                subscription.id,
                subscription.channelId,
                categories,
                ordering
            )

        } else {
            log.warn("Saw and ignored a syndEntry with no url: $syndEntry")
            return null
        }
    }

    private fun extractUrl(syndEntry: SyndEntry): String? {
        val url: String? = syndEntry.link
        val resolved = urlResolverService.resolveUrl(url)
        return if (resolved != null) {
            urlCleaner.cleanSubmittedItemUrl(resolved)
        } else {
            log.warn("Could not resolve url: $url")
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