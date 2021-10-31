package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.apache.logging.log4j.LogManager
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
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.rss.RssSubscriptionManager
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class SubscriptionsUIController @Autowired constructor(val usersDAO: UsersDAO, val channelsDAO: ChannelsDAO,
                                                       val subscriptionsDAO: SubscriptionsDAO,
                                                       val feedItemDAO: FeedItemDAO,
                                                       val feedItemPopulator: FeedItemPopulator,
                                                       val rssSubscriptionManager: RssSubscriptionManager,
                                                       val rssPoller: RssPoller,
                                                       val urlBuilder: UrlBuilder,
                                                       currentUserService: CurrentUserService,
                                                       response: HttpServletResponse,
                                                       request: HttpServletRequest) : WithSignedInUser(currentUserService, response, request) {

    private val log = LogManager.getLogger(SubscriptionsUIController::class.java)

    @GetMapping("/ui/subscriptions/{channel}/new")
    fun newSubscriptionForm(@PathVariable channelId: String): ModelAndView? {
        fun withChannelForUser(channelId: String, user: User, handler: (Channel) -> ModelAndView): ModelAndView? {
            val channel: Channel? = channelsDAO.getById(channelId)
            if (channel == null) {
                response.sendError(HttpStatus.NOT_FOUND.value())
                return null
            }
            if (user.username != channel.username) {    // TODO match by ids
                response.sendError(HttpStatus.FORBIDDEN.value())
                return null
            }
            return handler(channel)
        }

        return forCurrentUser { user ->
            withChannelForUser(channelId, user) { channel ->
                ModelAndView("newSubscription").
                addObject("username", user.username).
                addObject("channel", channel)
            }
        }
    }

    @PostMapping("/ui/subscriptions/feeds")
    fun addFeedSubscription(@RequestParam url: String, @RequestParam channel: String): ModelAndView? {
        fun executeAddSubscription(user: User): ModelAndView? {
            // TODO form binding and validation
            val subscription = rssSubscriptionManager.requestFeedSubscription(url, channel, user.username)
            subscriptionsDAO.add(subscription)
            log.info("Added subscription: $subscription")
            rssPoller.requestRead(subscription)
            return ModelAndView(RedirectView(urlBuilder.getSubscriptionUrl(subscription.id)))
        }
        return forCurrentUser(::executeAddSubscription)
    }

    @GetMapping("/ui/subscriptions/{id}")
    fun subscription(@PathVariable id: String, @RequestParam(required = false) page: Int?): ModelAndView? {
        fun renderSubscriptionPage(user: User): ModelAndView? {
            val subscription = subscriptionsDAO.getById(id) ?: return null

            val subscriptionChannel = channelsDAO.getById(subscription.channelId)
            val feedItemsResult = feedItemDAO.getSubscriptionFeedItems(subscription, page)

            val mv = ModelAndView("subscription")
            feedItemPopulator.populateFeedItems(feedItemsResult, mv, "feedItems")
            mv.addObject("user", user)
                    .addObject("channel", subscriptionChannel)
                    .addObject("subscription", subscription)
            return mv
        }

        return forCurrentUser(::renderSubscriptionPage)
    }

    @GetMapping("/ui/subscriptions/{id}/read")
    fun subscriptionRead(@PathVariable id: String): ModelAndView? {
        fun executeReload(user: User): ModelAndView? {
            val subscription = subscriptionsDAO.getByRssSubscriptionById(id)
            if (subscription != null) {
                rssPoller.requestRead(subscription)
                return ModelAndView(RedirectView(urlBuilder.getSubscriptionUrl(subscription.id)))
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            }
        }
        return forCurrentUser(::executeReload)
    }

}