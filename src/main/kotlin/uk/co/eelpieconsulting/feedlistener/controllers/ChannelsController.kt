package uk.co.eelpieconsulting.feedlistener.controllers

import com.google.common.base.Strings
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.ui.WithSignedInUser
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class ChannelsController @Autowired constructor(val channelsDAO: ChannelsDAO,
                                                val viewFactory: ViewFactory,
                                                val subscriptionsDAO: SubscriptionsDAO,
                                                val urlBuilder: UrlBuilder,
                                                val feedItemPopulator: FeedItemPopulator,
                                                val feedItemDAO: FeedItemDAO,
                                                currentUserService: CurrentUserService,
                                                response: HttpServletResponse,
                                                request: HttpServletRequest) : WithSignedInUser(currentUserService, response, request) {

    private val log = LogManager.getLogger(ChannelsController::class.java)

    private val X_TOTAL_COUNT = "X-Total-Count"

    @GetMapping("/{username}/channels")
    fun channelsJson(@PathVariable username: String): ModelAndView {
        fun renderChannels(user: User): ModelAndView {
            return ModelAndView(viewFactory.getJsonView()).addObject("data", channelsDAO.getChannelsFor(user))
        }
        return forCurrentUser(::renderChannels)
    }

    @GetMapping("/channels/{id}")
    fun channel(@PathVariable id: String): ModelAndView {
        return forCurrentUser {
            val channel = channelsDAO.getById(id)
            ModelAndView(viewFactory.getJsonView()).addObject("data", channel)
        }
    }

    @PostMapping("/channels")
    fun createSubscription(@RequestBody create: CreateChannelRequest): ModelAndView {
        fun createChannel(user: User): ModelAndView {
            log.info("Got channel create request: " + create)
            if (Strings.isNullOrEmpty(create.name)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Channel name is required")
            }

            // TODO check for existing channel with same name

            val channel = Channel(id = UUID.randomUUID().toString(), name = create.name, username = user.username)
            channelsDAO.save(channel)
            log.info("Added channel: $channel")

            return ModelAndView(viewFactory.getJsonView()).addObject("data", channel)
        }

        return forCurrentUser(::createChannel)
    }

    @GetMapping("/channels/{id}/subscriptions")
    fun channelSubscriptions(@PathVariable id: String, @RequestParam(required = false) url: String?): ModelAndView {
        fun renderChannels(user: User): ModelAndView {
            val channel = channelsDAO.getById(id)
            if (channel != null) {
                val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.id, url)
                return ModelAndView(viewFactory.getJsonView()).addObject("data", subscriptionsForChannel)
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found")
            }
        }
        return forCurrentUser(::renderChannels)
    }

    @GetMapping("/channels/{id}/items")
    fun channelJson(@PathVariable id: String,
                    @RequestParam(required = false) page: Int?,
                    @RequestParam(required = false) pageSize: Int?,
                    @RequestParam(required = false) format: String?,
                    @RequestParam(required = false) q: String?,
                    response: HttpServletResponse): ModelAndView {
        fun renderChannelItems(user: User): ModelAndView {
            val channel = channelsDAO.getById(id)
            if (channel != null) {
                var mv = ModelAndView(viewFactory.getJsonView())
                if (!Strings.isNullOrEmpty(format) && format == "rss") {    // TODO view factory could do this?
                    mv = ModelAndView(viewFactory.getRssView(channel.name, urlBuilder.getChannelUrl(channel), ""))
                }
                val results = feedItemDAO.getChannelFeedItemsResult(channel, page, q, pageSize)
                feedItemPopulator.populateFeedItems(results, mv, "data")
                val totalCount = results.totalCount
                response.addHeader(X_TOTAL_COUNT, java.lang.Long.toString(totalCount))
                return mv
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found")
            }
        }

        return forCurrentUser(::renderChannelItems)
    }

}