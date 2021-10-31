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
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.rss.RssSubscriptionManager
import javax.servlet.http.HttpServletRequest

@Controller
class SubscriptionsUIController @Autowired constructor(val usersDAO: UsersDAO, val channelsDAO: ChannelsDAO,
                                                       val subscriptionsDAO: SubscriptionsDAO,
                                                       val feedItemDAO: FeedItemDAO,
                                                       val feedItemPopulator: FeedItemPopulator,
                                                       val rssSubscriptionManager: RssSubscriptionManager,
                                                       val rssPoller: RssPoller,
                                                       val urlBuilder: UrlBuilder,
                                                       val conditionalLoads: ConditionalLoads,
                                                       currentUserService: CurrentUserService,
                                                       request: HttpServletRequest) : WithSignedInUser(currentUserService, request) {

    private val log = LogManager.getLogger(SubscriptionsUIController::class.java)

    @GetMapping("/ui/subscriptions/{channelId}/new")
    fun newSubscriptionForm(@PathVariable channelId: String): ModelAndView? {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(channelId, user) { channel ->
                ModelAndView("newSubscription").addObject("username", user.username).addObject("channel", channel)
            }
        }
    }

    @PostMapping("/ui/subscriptions/feeds")
    fun addFeedSubscription(@RequestParam url: String, @RequestParam(name = "channel") channelId: String): ModelAndView? {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(channelId, user) { channel ->
                // TODO form binding and validation
                val subscription = rssSubscriptionManager.requestFeedSubscription(url, channel.id, user.username)
                subscriptionsDAO.add(subscription)
                log.info("Added subscription: $subscription")
                rssPoller.requestRead(subscription)
                ModelAndView(RedirectView(urlBuilder.getSubscriptionUrl(subscription)))
            }
        }
    }

    @GetMapping("/ui/subscriptions/{id}")
    fun subscription(@PathVariable id: String, @RequestParam(required = false) page: Int?): ModelAndView? {
        fun renderSubscriptionPage(user: User): ModelAndView {
            val subscription = subscriptionsDAO.getById(id)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subsciption not found")

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
        fun executeReload(user: User): ModelAndView {
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