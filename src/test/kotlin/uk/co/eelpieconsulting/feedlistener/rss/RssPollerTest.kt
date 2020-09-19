package uk.co.eelpieconsulting.feedlistener.rss

import junit.framework.Assert.assertEquals
import org.joda.time.DateTime
import org.junit.Assert.assertNull
import org.junit.Test
import uk.co.eelpieconsulting.feedlistener.model.FeedItem

class RssPollerTest {

    private val feedItemLatestDateFinder = FeedItemLatestDateFinder()

    @Test
    fun canFindLatestFeedItemDateFromCollectionOfFeedItems() {
        assertNull(feedItemLatestDateFinder.getLatestItemDate(emptyList()))

        val recently = DateTime(2020, 1, 2, 3, 1, 1, 1)
        val mostRecent = DateTime(2020, 9, 8, 14, 1, 1, 1)
        val longAgo = DateTime(1980, 9, 8, 14, 1, 1, 1)

        val feedItemDates = listOf(recently, null, mostRecent, longAgo, null)
        val feedItems = feedItemDates.map { date ->
            // TODO this can be minimised when we move FeedItem to a Kotlin class
            FeedItem("title", "url", "body", date?.toDate(), null, null, null)
        }

        val latestItemDate = feedItemLatestDateFinder.getLatestItemDate(feedItems)
        assertEquals(mostRecent.toDate(), latestItemDate)
    }

}