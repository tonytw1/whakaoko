package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

@Component
class Classifier {

    fun classify(subscription: RssSubscription): String? {
        if (subscription.httpStatus == 200 || subscription.error == null) {
            return "ok"
        }
        if (subscription.httpStatus == 404) {
            return "gone"
        }
        return null
    }

}
