package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import java.util.*

class ClassifierTest {

    private val classifier = Classifier()

    @Test
    fun http200WithNoErrorIsOk() {
        val subscription = RssSubscription("http://localhost/ok", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 200
        subscription.error = null

        val result = classifier.classify(subscription)

        assertEquals("ok", result)
    }

    @Test
    fun http404IsGone() {
        val subscription = RssSubscription("http://localhost/gone", UUID.randomUUID().toString(), "a-user")
        subscription.httpStatus = 404

        val result = classifier.classify(subscription)

        assertEquals("gone", result)
    }

    @Test
    fun badHttpCodeButRecentContentIsWobbly() {
        // ie. the feed reading service has lost it's own connection. We don't want a back off when reading resumes.
        val subscription = RssSubscription("http://localhost/gone", UUID.randomUUID().toString(), "a-user")
        subscription.latestItemDate = DateTime.now().minusDays(1).toDate()
        subscription.httpStatus = -1

        val result = classifier.classify(subscription)

        assertEquals("wobbling", result)
    }

}