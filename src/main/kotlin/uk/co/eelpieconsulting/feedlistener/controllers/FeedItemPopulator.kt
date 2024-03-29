package uk.co.eelpieconsulting.feedlistener.controllers

import org.apache.commons.lang.StringEscapeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.controllers.ui.SubscriptionLabelService
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult
import java.util.stream.Collectors
import java.util.stream.Stream

@Component
class FeedItemPopulator @Autowired constructor(private val subscriptionLabelService: SubscriptionLabelService) {

    fun populateFeedItems(feedItemsResult: FeedItemsResult, mv: ModelAndView, field: String) {
        populate(mv, field, feedItemsResult.feedsItems)
        mv.addObject("totalCount", feedItemsResult.totalCount)
    }

    private fun populate(mv: ModelAndView, field: String, feedItems: List<FeedItem>) {
        // Some source feeds incorrectly over escape entities.
        // In these cases we will import and persist the feed items as provided and HTML unescape when outputting
        // This is repeated processing but makes for a reversible change and an accurate persisted representation of the source feed.
        val cleanedFeedItems: Stream<FeedItem> = feedItems.stream().map { feedItem: FeedItem -> overlyUnescape(feedItem) }
        val withSubscriptionNames = cleanedFeedItems.map { feedItem ->
            feedItem.copy(subscriptionName = subscriptionLabelService.labelForSubscription(feedItem.subscriptionId))
        }.collect(Collectors.toList())

        mv.addObject(field, withSubscriptionNames)
        mv.addObject("geotagged", geoTagged(withSubscriptionNames))
    }

    fun overlyUnescape(feedItem: FeedItem): FeedItem {
        return feedItem.copy(
            title = StringEscapeUtils.unescapeHtml(feedItem.title),
            body = StringEscapeUtils.unescapeHtml(feedItem.body)
        )
    }

    private fun geoTagged(feedItems: List<FeedItem>): List<FeedItem> {
        return feedItems.filter { it.isGeoTagged() }.toList()
    }

}