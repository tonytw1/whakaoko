package uk.co.eelpieconsulting.feedlistener.rss

import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class FeedParserTest {
    private val feedParser: FeedParser = FeedParser()
    
    @Test
    fun canParseFeedBytesIntoSyndFeed() {
        val input = readTestFile("wcc-news.xml")

        val result= feedParser.parseSyndFeed(input.toByteArray())

        assertEquals("Wellington City Council - News", result.get().title)
    }

    @Test
    fun needToStripNBSPFromMalformedFeeds() {
        val input = readTestFile("vinnies-news.xml")

        val result= feedParser.parseSyndFeed(input.toByteArray())

        assertEquals("Latest News - St Vincent de Paul Society Wellington", result.get().title)
    }

    @Test
    fun whatsUpWithCricketWellingtonsFeed() {
        val input = readTestFile("cricketwellington.xml")

        val result= feedParser.parseSyndFeed(input.toByteArray())

        assertEquals("Cricket Wellington", result.get().title)
        assertEquals(50, result.get().entries.size)
        val feedItemsIterator: Iterator<SyndEntry> = result.get().entries.iterator()
        val next: SyndEntry = feedItemsIterator.next()
        assertEquals("Big names return for Firebirds", next.title)
        assertEquals("21 Feb 2020 11:46:19 GMT", next.publishedDate.toGMTString())
    }

    private fun readTestFile(filename: String): String {
        return IOUtils.toString(
            FileInputStream(javaClass.classLoader.getResource(filename)!!.file),
            StandardCharsets.UTF_8
        )
    }
}
