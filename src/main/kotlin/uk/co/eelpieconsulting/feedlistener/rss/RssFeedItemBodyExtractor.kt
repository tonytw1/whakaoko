package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import com.sun.syndication.feed.synd.SyndContentImpl
import com.sun.syndication.feed.synd.SyndEntry
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component

@Component
class RssFeedItemBodyExtractor {

    private val log = LogManager.getLogger(RssFeedItemBodyExtractor::class.java)

    fun extractBody(syndEntry: SyndEntry): String? {
        val body = syndEntry.description?.value
        if (!Strings.isNullOrEmpty(body)) {
            return body
        }

        log.debug("No description found; looking for content")
        val contents = syndEntry.contents // contents returns an untyped list of SyndContentImpl
        if (contents.isNotEmpty()) {
            val firstItem = contents[0]
            if (firstItem != null && firstItem is SyndContentImpl) {
                return firstItem.value
            }
        }
        return null
    }

}