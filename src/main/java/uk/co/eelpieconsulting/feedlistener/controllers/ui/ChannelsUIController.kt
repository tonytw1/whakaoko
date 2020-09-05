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
class ChannelsUIController @Autowired constructor(val usersDAO: UsersDAO,
                                                  val subscriptionsDAO: SubscriptionsDAO,
                                                  val feedItemPopulator: FeedItemPopulator,
                                                  val channelsDAO: ChannelsDAO,
                                                  val feedItemDAO: FeedItemDAO,
                                                  currentUserService: CurrentUserService) : WithSignedInUser(currentUserService) {

    @GetMapping("/ui/{username}/channels/new")
    fun newChannelForm(): ModelAndView? {
        fun newChannelPage(user: User): ModelAndView {
            return ModelAndView("newChannel")
        }
        return forCurrentUser(::newChannelPage)
    }


    @GetMapping("/ui/{username}/channels/{id}")
    fun channel(@PathVariable username: String?,
                @PathVariable id: String?,
                @RequestParam(required = false) page: Int?,
                @RequestParam(required = false) q: String?
    ): ModelAndView? {
        val user = usersDAO.getByUsername(username)

        fun userChannelPage(user: User): ModelAndView {
            val channel = channelsDAO.getById(user.username, id)
            val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(username, channel.id, null)

            val mv = ModelAndView("channel").
            addObject("user", user).
            addObject("channel", channel).
            addObject("subscriptions", subscriptionsForChannel)

            if (!subscriptionsForChannel.isEmpty()) {
                val results = feedItemDAO.getChannelFeedItemsResult(username, channel, page, q, null)
                feedItemPopulator.populateFeedItems(results, mv, "inbox")

                val subscriptionCounts = subscriptionsForChannel.map { subscription ->
                    // TODO slow on channels with many subscriptions - cache or index?
                    val subscriptionFeedItemsCount = feedItemDAO.getSubscriptionFeedItemsCount(subscription.id)
                    Pair(subscription.id, subscriptionFeedItemsCount)
                }.toMap()

                mv.addObject("subscriptionCounts", subscriptionCounts)
            }
            return mv
        }

        return forCurrentUser(::userChannelPage)
    }

}