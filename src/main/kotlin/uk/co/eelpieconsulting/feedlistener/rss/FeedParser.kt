package uk.co.eelpieconsulting.feedlistener.rss

import com.github.kittinunf.result.Result
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.ParsingFeedException
import com.rometools.rome.io.SyndFeedInput
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream

@Component
class FeedParser {
    @Throws(FeedException::class)
    fun parseSyndFeed(bytes: ByteArray): Result<SyndFeed, Exception> {
        val cleaned = cleanLeadingWhitespace(bytes) // Some feeds have breaking whitespace before the XML

        // Attempt to parse feed
        val parsingResult = parse(cleaned)

        return parsingResult.fold({ syndFeed ->
            // If successful return the feed
            parsingResult

        }, { ex ->
            // Attempt to recover from malformed feed specific parsing errors
            when (ex) {
                is ParsingFeedException -> {
                    if (ex.message!!.contains("The entity \"nbsp\" was referenced, but not declared.")) {
                        val withOutNbsp = String(bytes).replace("\\&nbsp;".toRegex(), " ").toByteArray()
                        parse(withOutNbsp)
                    } else if (ex.message!!.contains("Invalid XML: Error on line 1: Content is not allowed in prolog")) {
                        val withUtf8 = String(bytes).replace("utf-16".toRegex(), "utf-8").toByteArray()
                        parse(withUtf8)
                    } else {
                        parsingResult
                    }
                }
                else -> parsingResult
            }
        })
    }

    private fun parse(bytes: ByteArray): Result<SyndFeed, Exception> {
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        val inputStream = InputSource(byteArrayInputStream)
        return try {
            val syndFeedInput = SyndFeedInput()
            val syndFeed = syndFeedInput.build(inputStream)
            IOUtils.closeQuietly(byteArrayInputStream)
            Result.success(syndFeed)

        } catch (e: FeedException) {
            IOUtils.closeQuietly(byteArrayInputStream)
            Result.Failure(e)
        }
    }

    private fun cleanLeadingWhitespace(bytes: ByteArray): ByteArray {
        val string = String(bytes)
        return if (string.trim { it <= ' ' } == string) {
            bytes
        } else string.trim { it <= ' ' }.toByteArray()
    }

}