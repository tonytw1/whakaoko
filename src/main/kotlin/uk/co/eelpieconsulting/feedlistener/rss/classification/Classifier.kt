package uk.co.eelpieconsulting.feedlistener.rss.classification

import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription

@Component
class Classifier {

    fun classify(subscription: RssSubscription): String {
        return "Unknown"
    }

}
