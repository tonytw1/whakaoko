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
import javax.servlet.http.HttpServletResponse

@Controller
class ChannelsUIController @Autowired constructor(val usersDAO: UsersDAO,
                                                  val subscriptionsDAO: SubscriptionsDAO,
                                                  val feedItemPopulator: FeedItemPopulator,
                                                  val channelsDAO: ChannelsDAO,
                                                  val feedItemDAO: FeedItemDAO,
                                                  currentUserService: CurrentUserService,
                                                  response: HttpServletResponse) : WithSignedInUser(currentUserService, response) {

    @GetMapping("/ui/channels/new")
    fun newChannelForm(): ModelAndView? {
        fun newChannelPage(user: User): ModelAndView {
            return ModelAndView("newChannel")
        }
        return forCurrentUser(::newChannelPage)
    }


    @GetMapping("/ui/channels/{id}")
    fun channel(@PathVariable id: String,
                @RequestParam(required = false) page: Int?,
                @RequestParam(required = false) q: String?
    ): ModelAndView? {
        fun userChannelPage(user: User): ModelAndView? {
            val channel = channelsDAO.getById(id)
            if (channel != null) {
                val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.id, null)

                val mv = ModelAndView("channel").addObject("user", user).addObject("channel", channel).addObject("subscriptions", subscriptionsForChannel)

                if (!subscriptionsForChannel.isEmpty()) {
                    val results = feedItemDAO.getChannelFeedItemsResult(channel, page, q, null)
                    feedItemPopulator.populateFeedItems(results, mv, "feedItems")
                }
                return mv

            } else {
                return null
            }
        }

        return forCurrentUser(::userChannelPage)
    }

}