package uk.co.eelpieconsulting.feedlistener.rss

import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import java.util.*

class RssPollerTest {

    private val feedItemLatestDateFinder = FeedItemLatestDateFinder()

    @Test
    fun canFindLatestFeedItemDateFromCollectionOfFeedItems() {
        assertNull(feedItemLatestDateFinder.getLatestItemDate(emptyList()))

        val recently = DateTime(2020, 1, 2, 3, 1, 1, 1)
        val mostRecent = DateTime(2020, 9, 8, 14, 1, 1, 1)
        val longAgo = DateTime(1980, 9, 8, 14, 1, 1, 1)

        val feedItemDates = listOf(recently, mostRecent, longAgo)
        val feedItems = feedItemDates.map { date ->
            FeedItem(
                ObjectId.get(),
                title = "title",
                url = "url", body = "body",
                date = date.toDate(),
                subscriptionId = UUID.randomUUID().toString(),
                channelId = UUID.randomUUID().toString(),
                _categories = null,
                ordering = date.toDate()
            )
        }

        val latestItemDate = feedItemLatestDateFinder.getLatestItemDate(feedItems)
        assertEquals(mostRecent.toDate(), latestItemDate)
    }

    @Test
    fun canNameRootCauseOfFeedFetchingExceptions() {
        val feedFetchingException =
            FeedFetchingException(message = "Something", rootCause = RuntimeException("Something went wrong"))

        assertEquals("Something", feedFetchingException.message)
        val rootCauseName = feedFetchingException.rootCause?.javaClass?.simpleName
        assertEquals("RuntimeException", rootCauseName)
    }

}
