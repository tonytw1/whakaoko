package uk.co.eelpieconsulting.feedlistener.rss.images

import com.google.common.base.Strings
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.logging.log4j.LogManager
import org.htmlparser.NodeFilter
import org.htmlparser.Parser
import org.htmlparser.Tag
import org.htmlparser.filters.TagNameFilter
import org.htmlparser.util.NodeList
import org.htmlparser.util.ParserException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.rss.RssFeedItemBodyExtractor

@Component
class BodyHtmlImageExtractor @Autowired constructor(private val rssFeedItemBodyExtractor: RssFeedItemBodyExtractor) {

    private val log = LogManager.getLogger(BodyHtmlImageExtractor::class.java)

    fun extractImageFrom(item: SyndEntry): String? {
        val itemBody = rssFeedItemBodyExtractor.extractBody(item)
        return if (itemBody != null &&!Strings.isNullOrEmpty(itemBody)) {
            val imageSrc = extractImage(itemBody)
            if (!Strings.isNullOrEmpty(imageSrc)) imageSrc else null
        } else {
            null
        }
    }

    private fun extractImage(itemBody: String): String? {
        return try {
            val parser = Parser()
            parser.setInputHTML(itemBody)
            val tagNameFilter: NodeFilter = TagNameFilter("img")
            val imageNodes: NodeList = parser.extractAllNodesThatMatch(tagNameFilter)
            log.debug("Found images: " + imageNodes.size())
            extractFirstImage(imageNodes)
        } catch (e: ParserException) {
            log.warn("Failed to parse item body for images", e)
            null
        }
    }

    private fun extractFirstImage(imageNodes: NodeList): String? {
        if (imageNodes.size() == 0) {
            return null
        }
        val imageTag = imageNodes.elementAt(0) as Tag
        val imageSrc = imageTag.getAttribute("src")
        log.debug("Found first image: " + imageTag.toHtml() + ", " + imageSrc)
        return imageSrc
    }

}
