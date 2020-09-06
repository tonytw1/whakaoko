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
import uk.co.eelpieconsulting.feedlistener.model.User

@Controller
class SubscriptionsUIController @Autowired constructor(val usersDAO: UsersDAO, val channelsDAO: ChannelsDAO,
                                                       val subscriptionsDAO: SubscriptionsDAO,
                                                       val feedItemDAO: FeedItemDAO,
                                                       val feedItemPopulator: FeedItemPopulator,
                                                       currentUserService: CurrentUserService) : WithSignedInUser(currentUserService) {

    @GetMapping("/ui/subscriptions/new")
    fun newSubscriptionForm(): ModelAndView? {
        fun newChannelPrompt(user: User): ModelAndView {
            return ModelAndView("newSubscription").
            addObject("username", user.username).
            addObject("channels", channelsDAO.getChannels(user.username))
        }

        return forCurrentUser(::newChannelPrompt)
    }

    @GetMapping("/ui/subscriptions/{id}")
    fun subscription(@PathVariable id: String?, @RequestParam(required = false) page: Int?): ModelAndView? {
        fun meh(user: User): ModelAndView {
            val subscription = subscriptionsDAO.getById(id)
                    ?: throw RuntimeException("Invalid subscription")

            val channel = if (user.username != null && subscription.channelId != null) {
                channelsDAO.getById(subscription.username, subscription.channelId)
            } else {
                null
            }

            val feedItemsResult = feedItemDAO.getSubscriptionFeedItems(subscription, page)

            val mv = ModelAndView("subscription")
            feedItemPopulator.populateFeedItems(feedItemsResult, mv, "feedItems")
            mv.addObject("user", user)
                    .addObject("channel", channel).addObject("subscription", subscription)
            return mv
        }

        return forCurrentUser(::meh)
    }


}