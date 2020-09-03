package uk.co.eelpieconsulting.feedlistener.controllers.ui

import com.google.common.collect.Maps
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

@Controller
class ChannelController @Autowired constructor(val usersDAO: UsersDAO,
                                               val subscriptionsDAO: SubscriptionsDAO,
                                               val feedItemPopulator: FeedItemPopulator,
                                               val channelsDAO: ChannelsDAO,
                                               val feedItemDAO: FeedItemDAO) {

    @GetMapping("/ui/{username}/channels/new")
    fun newChannelForm(): ModelAndView? {
        return ModelAndView("newChannel")
    }

    @GetMapping("/ui/{username}/channels/{id}")
    fun channel(@PathVariable username: String?,
                @PathVariable id: String?,
                @RequestParam(required = false) page: Int?,
                @RequestParam(required = false) q: String?
    ): ModelAndView? {
        val user = usersDAO.getByUsername(username)
        val channel = channelsDAO.getById(user.username, id)
        val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(username, channel.id, null)

        val mv = ModelAndView("channel").
        addObject("user", user).
        addObject("channel", channel).
        addObject("subscriptions", subscriptionsForChannel)

        if (!subscriptionsForChannel.isEmpty()) {
            val totalCount = feedItemPopulator.populateFeedItems(username, channel, page, mv, "inbox", q)
            mv.addObject("inboxSize", totalCount)
            val subscriptionCounts: MutableMap<String, Long> = Maps.newHashMap()
            for (subscription in subscriptionsForChannel) {
                subscriptionCounts[subscription.id] = feedItemDAO.getSubscriptionFeedItemsCount(subscription.id) // TODO slow on channels with many subscriptions - cache or index?
            }
            mv.addObject("subscriptionCounts", subscriptionCounts)
        }
        return mv
    }

}