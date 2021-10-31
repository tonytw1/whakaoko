package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.IdBuilder
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class ChannelsUIController @Autowired constructor(val usersDAO: UsersDAO,
                                                  val subscriptionsDAO: SubscriptionsDAO,
                                                  val feedItemPopulator: FeedItemPopulator,
                                                  val channelsDAO: ChannelsDAO,
                                                  val feedItemDAO: FeedItemDAO,
                                                  val idBuilder: IdBuilder,
                                                  val urlBuilder: UrlBuilder,
                                                  currentUserService: CurrentUserService,
                                                  response: HttpServletResponse,
                                                  request: HttpServletRequest) : WithSignedInUser(currentUserService, response, request) {

    @GetMapping("/ui/channels/new")
    fun newChannelForm(): ModelAndView? {
        fun newChannelPage(user: User): ModelAndView {
            return ModelAndView("newChannel")
        }
        return forCurrentUser(::newChannelPage)
    }

    @PostMapping("/ui/channels/new")
    fun addChannel(@RequestParam name: String): ModelAndView? {
        fun executeAddChannel(user: User): ModelAndView {
            val newChannel = Channel(idBuilder.makeIdFor(name), name, user.username)
            channelsDAO.add(user, newChannel)
            return ModelAndView(RedirectView(urlBuilder.getChannelUrl(newChannel)))
        }

        return forCurrentUser(::executeAddChannel)
    }

    @GetMapping("/ui/channels/{channelId}")
    fun channel(@PathVariable channelId: String,
                @RequestParam(required = false) page: Int?,
                @RequestParam(required = false) q: String?
    ): ModelAndView {
        fun userChannelPage(user: User): ModelAndView {
            return withChannelForUser(channelId, user) { channel ->
                val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.id, null)
                val mv = ModelAndView("channel").
                addObject("channel", channel).
                addObject("subscriptions", subscriptionsForChannel)
                if (!subscriptionsForChannel.isEmpty()) {
                    val results = feedItemDAO.getChannelFeedItemsResult(channel, page, q, null)
                    feedItemPopulator.populateFeedItems(results, mv, "feedItems")
                }
                mv
            }
        }
        return forCurrentUser(::userChannelPage)
    }

    // TODO duplication
    private fun withChannelForUser(channelId: String, user: User, handler: (Channel) -> ModelAndView): ModelAndView {
        val channel: Channel? = channelsDAO.getById(channelId)
        if (channel == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found")
        }
        if (user.username != channel.username) {    // TODO match by ids
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Channel does not belong to this user")
        }
        return handler(channel)
    }

}