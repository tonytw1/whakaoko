package uk.co.eelpieconsulting.feedlistener.views

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.EtagGenerator
import uk.co.eelpieconsulting.common.views.json.JsonSerializer
import uk.co.eelpieconsulting.common.views.json.JsonView
import uk.co.eelpieconsulting.feedlistener.TestData
import uk.co.eelpieconsulting.feedlistener.model.Channel
import java.util.*

class JsonViewTest : TestData {

    @Test
    fun jsonDatesShouldBeInHumanReadableTimeZonedFormat() {
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")
        val subscription = testSubscription(channel)

        val view = JsonView(JsonSerializer(ObjectMapper().registerKotlinModule()), EtagGenerator())

        val mv = ModelAndView(view)
        mv.addObject("data", testFeedItemFor(subscription))
        val response = MockHttpServletResponse()

        view.render(mv.model, MockHttpServletRequest(), response)

        val responseBody = response.contentAsString
        val responseFields = ObjectMapper().readValue(responseBody, Map::class.java)
        assertEquals("2023-05-02T12:23:00Z", responseFields["date"])
        assertEquals("2023-05-02T12:23:00Z", responseFields["accepted"])
    }

}