package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class ClassifierTest {

    @Test
    fun http200WithNoErrorIsOk() {
        val subscription = RssSubscription("http://localhost/ok", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 200
        subscription.error = null

        val result = Classifier().classify(subscription)

        assertEquals("ok", result)
    }

    @Test
    fun http404IsGone() {
        val subscription = RssSubscription("http://localhost/gone", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 404

        val result = Classifier().classify(subscription)

        assertEquals("gone", result)
    }

}