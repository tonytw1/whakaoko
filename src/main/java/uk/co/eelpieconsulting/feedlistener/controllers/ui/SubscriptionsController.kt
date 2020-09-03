package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult

@Controller
class SubscriptionsController @Autowired constructor(val usersDAO: UsersDAO, val channelsDAO: ChannelsDAO,
                                                     val subscriptionsDAO: SubscriptionsDAO,
                                                     val feedItemDAO: FeedItemDAO,
                                                     val feedItemPopulator: FeedItemPopulator) {

    @GetMapping("/ui/{username}/subscriptions/new")
    fun newSubscriptionForm(@PathVariable username: String?): ModelAndView? {
        val user = usersDAO.getByUsername(username)
        return ModelAndView("newSubscription").addObject("username", user.username).addObject("channels", channelsDAO.getChannels(user.username))
    }

    @GetMapping("/ui/{username}/subscriptions/{id}")
    fun subscription(@PathVariable username: String?, @PathVariable id: String?,
                     @RequestParam(required = false) page: Int?): ModelAndView? {
        val user = usersDAO.getByUsername(username)
        val subscription = subscriptionsDAO.getById(user.username, id) ?: throw RuntimeException("Invalid subscription")

        var channel: Channel? = null
        if (user.username != null && subscription.channelId != null) {
            channel = channelsDAO.getById(subscription.username, subscription.channelId)
        }

        val feedItemsResult: FeedItemsResult = if (page != null)
            feedItemDAO.getSubscriptionFeedItems(subscription.id, FeedItemPopulator.MAX_FEED_ITEMS, page)
        else feedItemDAO.getSubscriptionFeedItems(subscription.id, FeedItemPopulator.MAX_FEED_ITEMS)

        val mv = ModelAndView("subscription")
        feedItemPopulator.populateFeedItems(feedItemsResult, mv, "feedItems")
        mv.addObject("user", user)
                .addObject("channel", channel).addObject("subscription", subscription)
                .addObject("subscriptionSize", feedItemsResult.totalCount)  // TODO push into populate call?
        return mv
    }

}