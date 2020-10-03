package uk.co.eelpieconsulting.feedlistener.rss

import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.io.FeedException
import com.sun.syndication.io.ParsingFeedException
import com.sun.syndication.io.SyndFeedInput
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.InputStream

@Component
class FeedParser {
    @Throws(FeedException::class)
    fun parseSyndFeed(bytes: ByteArray): SyndFeed {
        val cleaned = cleanLeadingWhitespace(bytes)
        return try {
            parse(cleaned)
        } catch (p: ParsingFeedException) {
            if (p.message!!.contains("The entity \"nbsp\" was referenced, but not declared.")) {
                val withOutNbsp = String(bytes).replace("\\&nbsp;".toRegex(), " ").toByteArray()
                parse(withOutNbsp)
            } else if (p.message!!.contains("Invalid XML: Error on line 1: Content is not allowed in prolog")) {
                val withUtf8 = String(bytes).replace("utf-16".toRegex(), "utf-8").toByteArray()
                parse(withUtf8)
            } else {
                throw p
            }
        }
    }

    @Throws(FeedException::class)
    private fun parse(bytes: ByteArray): SyndFeed {
        val byteArrayInputStream: InputStream = ByteArrayInputStream(bytes)
        val inputStream = InputSource(byteArrayInputStream)
        return try {
            val syndFeedInput = SyndFeedInput(false)
            val syndFeed = syndFeedInput.build(inputStream)
            IOUtils.closeQuietly(byteArrayInputStream)
            syndFeed
        } catch (e: FeedException) {
            IOUtils.closeQuietly(byteArrayInputStream)
            throw e
        }
    }

    private fun cleanLeadingWhitespace(bytes: ByteArray): ByteArray {
        return try {
            val string = String(bytes)
            if (string.trim { it <= ' ' } == string) {
                bytes
            } else string.trim { it <= ' ' }.toByteArray()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}