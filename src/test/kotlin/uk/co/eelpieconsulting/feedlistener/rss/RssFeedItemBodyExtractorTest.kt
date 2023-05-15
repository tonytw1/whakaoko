package uk.co.eelpieconsulting.feedlistener.rss

import com.github.kittinunf.result.Result
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class RssFeedItemBodyExtractorTest {

    private val rssFeedItemBodyExtractor = RssFeedItemBodyExtractor()

    @Test
    fun canExtractBodyFromItemDescriptionTag() {
        val firstEntry = asSyndFeed("wcc-news.xml").get().entries.get(0) as SyndEntry

        val extractedBody = rssFeedItemBodyExtractor.extractBody(firstEntry)

        assertEquals("Te Wiki o te reo Māori is a nationwide week-long celebration of te reo Māori, and Wellington City Council will be supporting it next week with events and activities online and around the Capital.", extractedBody)
    }

    @Test
    fun shouldPreferDescriptionOverContent() {
        // Feed items have a brief description field and an optional content:encoded which may contain the full
        // content of the item
        val firstEntry = asSyndFeed("adam-art-gallery.xml").get().entries.get(0) as SyndEntry

        val extractedBody = rssFeedItemBodyExtractor.extractBody(firstEntry)

        assertEquals("Image ProcessorsArtists in the Medium – A Short History 1968–2020 Te Pātaka Toi Adam Art Gallery14 September – 7 November 2021 Curated by Christina Barton Featuring works by Aldo Tambellini, Richard Serra, Dara Birnbaum, Martha Rosler with Paper Tiger TV, Harun Farocki, Lisa Reihana, Megan Dunn, Ryan Trecartin and Lizzie Fitch, Wynne Greenwood and K8 [&#8230;]", extractedBody)
    }

    private fun asSyndFeed(filename: String): Result<SyndFeed, Exception> {
        val input = IOUtils.toString(FileInputStream(this.javaClass.classLoader.getResource(filename)!!.file), StandardCharsets.UTF_8)
        val feedParser = FeedParser()
        return feedParser.parseSyndFeed(input.toByteArray())
    }

}