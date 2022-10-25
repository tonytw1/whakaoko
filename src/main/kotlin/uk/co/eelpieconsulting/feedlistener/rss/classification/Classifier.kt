package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.joda.time.DateTime
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

@Component
class Classifier {

    private val goodHttpCodes = setOf(200, 304)
    private val badHttpCodes = setOf(404, 401, -1)

    fun classify(subscription: RssSubscription): FeedStatus? {
        if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error == null) {
            return FeedStatus.ok
        }

        if (goodHttpCodes.contains(subscription.httpStatus) && subscription.error != null) {
            return FeedStatus.broken
        }

        if (badHttpCodes.contains(subscription.httpStatus)) {
            if (subscription.latestItemDate == null) {
                return FeedStatus.gone

            } else {
                return if (DateTime(subscription.latestItemDate).plusDays(3).isAfterNow) FeedStatus.wobbling else FeedStatus.gone
            }
        }

        return null
    }

}
