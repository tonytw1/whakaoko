package uk.co.eelpieconsulting.feedlistener.views

import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.EtagGenerator
import uk.co.eelpieconsulting.feedlistener.model.Category
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class JsonViewTest {

    @Test
    fun jsonDatesShouldBeInHumanReadableTimeZonedFormat() {
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
        val subscription = testSubscription(channel)

        val view = JsonView(JsonSerializer(), EtagGenerator())

        val mv = ModelAndView(view)
        mv.addObject("data", testFeedItemFor(subscription))
        val response = MockHttpServletResponse()

        view.render(mv.model, MockHttpServletRequest(), response)

        val responseBody = response.contentAsString
        assertTrue(responseBody.contains("\"date\":\"2023-05-02T11:23:00Z\""))
        assertTrue(responseBody.contains("\"accepted\":\"2023-05-02T11:23:00Z\""))
    }

    private fun testSubscription(channel: Channel): RssSubscription {
        val subscription = RssSubscription(url = "http://localhost/rss", channelId = channel.id, username = "a-user")
        subscription.id = UUID.randomUUID().toString()
        subscription.channelId = channel.id
        return subscription
    }

    private fun testFeedItemFor(subscription: RssSubscription, categories: List<Category>? = null): FeedItem {
        val url = "http://localhost/" + UUID.randomUUID().toString()
        val date = DateTime(2023, 5, 2, 12, 23)
        return FeedItem(
            ObjectId.get(),
            UUID.randomUUID().toString(),
            url,
            null,
            date.toDate(),
            date.toDate(),
            null,
            null,
            null,
            subscription.id,
            subscription.channelId,
            categories,
            date.toDate()
        )
    }

}