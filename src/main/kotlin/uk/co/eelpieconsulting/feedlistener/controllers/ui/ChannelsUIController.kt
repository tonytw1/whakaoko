package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.IdBuilder
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.ConditionalLoads
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletRequest

@Controller
class ChannelsUIController @Autowired constructor(val usersDAO: UsersDAO,
                                                  val subscriptionsDAO: SubscriptionsDAO,
                                                  val feedItemPopulator: FeedItemPopulator,
                                                  val channelsDAO: ChannelsDAO,
                                                  val feedItemDAO: FeedItemDAO,
                                                  val idBuilder: IdBuilder,
                                                  val urlBuilder: UrlBuilder,
                                                  val conditionalLoads: ConditionalLoads,
                                                  currentUserService: CurrentUserService,
                                                  request: HttpServletRequest) : WithSignedInUser(currentUserService, request) {

    @GetMapping("/ui/channels/new")
    fun newChannelPrompt(): ModelAndView {
        return forCurrentUser { ModelAndView("newChannel") }
    }

    @PostMapping("/ui/channels/new")
    fun addChannel(@RequestParam name: String): ModelAndView {
        fun executeAddChannel(user: User): ModelAndView {
            val proposedId = idBuilder.makeIdForChannel()
            val newChannel = Channel(proposedId, name, user.username)
            if (channelsDAO.usersChannelByName(user, name) == null) {
                channelsDAO.add(user, newChannel)
                return ModelAndView(RedirectView(urlBuilder.getChannelUrl(newChannel)))
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Channel with same name already exists")
            }
        }
        return forCurrentUser(::executeAddChannel)
    }

    @GetMapping("/ui/channels/{channelId}")
    fun channel(@PathVariable channelId: String,
                @RequestParam(required = false) page: Int?,
                @RequestParam(required = false) q: String?
    ): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(channelId, user) { channel ->
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
    }

}