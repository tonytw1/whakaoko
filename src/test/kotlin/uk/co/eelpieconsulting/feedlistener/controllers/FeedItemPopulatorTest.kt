package uk.co.eelpieconsulting.feedlistener.controllers

import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import uk.co.eelpieconsulting.feedlistener.controllers.ui.SubscriptionLabelService
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import java.util.*

class FeedItemPopulatorTest {
    private val subscriptionLabelService = Mockito.mock(
        SubscriptionLabelService::class.java
    )

    @Test
    fun canCorrectForExcessivelyEscapedInputFeedItems() {
        val incorrectlyEscapedHeadline = "St John&#39;s Bar, 5 Cable Street, Te Aro, Wellington"
        val feedItem = FeedItem(
            objectId = ObjectId.get(),
            title = incorrectlyEscapedHeadline, url = "http://localhost", date = DateTime.now().toDate(), body = null,
            subscriptionId = UUID.randomUUID().toString(), channelId = UUID.randomUUID().toString(), _categories = emptyList(), ordering = DateTime.now().toDate()
        )
        val fixed = FeedItemPopulator(subscriptionLabelService).overlyUnescape(feedItem)
        Assertions.assertEquals("St John's Bar, 5 Cable Street, Te Aro, Wellington", fixed.headline)
    }

}
