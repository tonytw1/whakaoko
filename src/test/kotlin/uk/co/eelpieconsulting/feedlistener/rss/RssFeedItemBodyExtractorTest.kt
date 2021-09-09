package uk.co.eelpieconsulting.feedlistener.rss

import com.sun.syndication.feed.synd.SyndEntry
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.FileInputStream

class RssFeedItemBodyExtractorTest {

    private val rssFeedItemBodyExtractor = RssFeedItemBodyExtractor()

    @Test
    fun canExtractBodyFromItemDescriptionTag() {
        val input = IOUtils.toString(FileInputStream(this.javaClass.classLoader.getResource("wcc-news.xml").file))
        val feedParser = FeedParser()
        val result = feedParser.parseSyndFeed(input.toByteArray())
        val firstEntry = result.get().entries.get(0) as SyndEntry

        val extractBody = rssFeedItemBodyExtractor.extractBody(firstEntry)

        assertEquals("Te Wiki o te reo Māori is a nationwide week-long celebration of te reo Māori, and Wellington City Council will be supporting it next week with events and activities online and around the Capital.", extractBody)
    }

    // Feed items have a brief description and a full fat content:encoded

}