package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

@Component
class Classifier {

    private val goodHttpCodes = setOf(200, 304)
    private val badHttpCodes = setOf(404, 401, -1)

    fun classify(subscription: RssSubscription): String? {
        if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error == null) {
            return "ok"
        }

        if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error != null) {
            return "broken"
        }

        if (badHttpCodes.contains(subscription.httpStatus)) {
            return "gone"
        }
        return null
    }

}
