package uk.co.eelpieconsulting.feedlistener.controllers

import com.google.common.base.Strings
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.IdBuilder
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.ui.WithSignedInUser
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User

@Controller
class ChannelsController @Autowired constructor(private val channelsDAO: ChannelsDAO,
                                                private val viewFactory: ViewFactory,
                                                val subscriptionsDAO: SubscriptionsDAO,
                                                val urlBuilder: UrlBuilder,
                                                private val feedItemPopulator: FeedItemPopulator,
                                                private val feedItemDAO: FeedItemDAO,
                                                private val conditionalLoads: ConditionalLoads,
                                                private val idBuilder: IdBuilder,
                                                currentUserService: CurrentUserService,
                                                request: HttpServletRequest) : WithSignedInUser(currentUserService, request) {

    private val log = LogManager.getLogger(ChannelsController::class.java)

    private val X_TOTAL_COUNT = "X-Total-Count"

    @CrossOrigin
    @GetMapping("/{username}/channels")
    fun channelsJson(@PathVariable username: String): ModelAndView {
        fun renderChannels(user: User): ModelAndView {
            if (user.username != username) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot list another users channels")
            }
            return ModelAndView(viewFactory.getJsonView()).addObject("data", channelsDAO.getChannelsFor(user))
        }
        return forCurrentUser(::renderChannels)
    }

    @CrossOrigin
    @GetMapping("/channels/{id}")
    fun channel(@PathVariable id: String): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(id, user) { channel ->
                ModelAndView(viewFactory.getJsonView()).addObject("data", channel)
            }
        }
    }

    @CrossOrigin
    @PostMapping("/channels")
    fun createSubscription(@RequestBody create: CreateChannelRequest): ModelAndView {
        fun createChannel(user: User): ModelAndView {
            log.info("Got channel create request: " + create)
            if (Strings.isNullOrEmpty(create.name)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Channel name is required")
            }

            if (channelsDAO.usersChannelByName(user, create.name) == null) {
                val newChannel = Channel(id = idBuilder.makeIdForChannel(), name = create.name, username = user.username)
                channelsDAO.save(newChannel)
                log.info("Added channel: $newChannel")
                return ModelAndView(viewFactory.getJsonView()).addObject("data", newChannel)
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create channel for anonymous user")
            }
        }

        return forCurrentUser(::createChannel)
    }

    @CrossOrigin
    @GetMapping("/channels/{id}/subscriptions")
    fun channelSubscriptions(@PathVariable id: String, @RequestParam(required = false) url: String?): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(id, user) { channel ->
                val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.id, url)
                ModelAndView(viewFactory.getJsonView()).addObject("data", subscriptionsForChannel)
            }
        }
    }

    @CrossOrigin
    @GetMapping("/channels/{id}/items")
    fun channelJson(@PathVariable id: String,
                    @RequestParam(required = false) page: Int?,
                    @RequestParam(required = false) pageSize: Int?,
                    @RequestParam(required = false) format: String?,
                    @RequestParam(required = false) q: String?,
                    @RequestParam(required = false) subscriptions: List<String>?,
                    response: HttpServletResponse): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(id, user) { channel ->
                var mv = ModelAndView(viewFactory.getJsonView())
                if (!Strings.isNullOrEmpty(format) && format == "rss") {    // TODO view factory could do this?
                    mv = ModelAndView(viewFactory.getRssView(channel.name, urlBuilder.getChannelUrl(channel), ""))
                }
                val results = feedItemDAO.getChannelFeedItemsResult(channel, page, q, pageSize, subscriptions)
                feedItemPopulator.populateFeedItems(results, mv, "data")
                val totalCount = results.totalCount
                response.addHeader(X_TOTAL_COUNT, totalCount.toString())
                mv
            }
        }
    }

}