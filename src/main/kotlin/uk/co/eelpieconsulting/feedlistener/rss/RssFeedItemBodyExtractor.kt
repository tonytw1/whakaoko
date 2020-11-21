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
        val contents = syndEntry.contents // contents returns an untyped list of SyndContentImpl
        if (contents.isNotEmpty()) {
            val firstItem = contents[0]
            if (firstItem != null && firstItem is SyndContentImpl) {
                return firstItem.value
            }
        }

        log.debug("No content body found; looking for description")
        val body = getItemDescription(syndEntry)
        return if (!Strings.isNullOrEmpty(body)) {
            body
        } else null
    }

    private fun getItemDescription(syndEntry: SyndEntry): String? {
        return syndEntry.description?.value
    }

}