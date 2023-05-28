package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.joda.time.DateTime
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

@Component
class Classifier {

    private val goodHttpCodes = setOf(200, 304)
    private val badHttpCodes = setOf(404, 401, -1)

    fun classify(subscription: RssSubscription): FeedStatus? {
        return livenessStatus(subscription)
    }

    private fun livenessStatus(subscription: RssSubscription) =
        if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error == null) {
            FeedStatus.ok
        } else if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error != null) {
            FeedStatus.broken
        } else if (badHttpCodes.contains(subscription.httpStatus)) {
            if (subscription.latestItemDate == null) {
                FeedStatus.gone
            } else {
                if (DateTime(subscription.latestItemDate).plusDays(3).isAfterNow) FeedStatus.wobbling else FeedStatus.gone
            }
        } else {
            null
        }

}
