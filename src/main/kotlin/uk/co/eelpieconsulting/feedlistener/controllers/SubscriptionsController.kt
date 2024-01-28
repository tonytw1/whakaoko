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
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.ui.WithSignedInUser
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.views.ViewFactory

@Controller
class SubscriptionsController @Autowired constructor(
    private val subscriptionsDAO: SubscriptionsDAO,
    private val feedItemPopulator: FeedItemPopulator,
    private val feedItemDAO: FeedItemDAO,
    private val viewFactory: ViewFactory,
    private val urlBuilder: UrlBuilder,
    private val rssPoller: RssPoller,
    private val conditionalLoads: ConditionalLoads,
    currentUserService: CurrentUserService,
    request: HttpServletRequest
) : WithSignedInUser(currentUserService, request) {

    private val log = LogManager.getLogger(ChannelsController::class.java)

    private val xTotalCount = "X-Total-Count"

    @CrossOrigin
    @GetMapping("/subscriptions/{id}/items")
    fun subscriptionItems(
        @PathVariable id: String,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
        @RequestParam(required = false) format: String?,
        response: HttpServletResponse
    ): ModelAndView {

        fun renderSubscriptionItems(user: User): ModelAndView {
            return conditionalLoads.withSubscriptionForUser(id, user) { subscription ->
                val mv = if (!Strings.isNullOrEmpty(format) && format == "rss") {
                    val title = if (!Strings.isNullOrEmpty(subscription.name)) subscription.name else subscription.id
                    ModelAndView(viewFactory.rssView(title, urlBuilder.getSubscriptionUrl(subscription), ""))
                } else {
                    ModelAndView(viewFactory.jsonView())
                }
                val feedItemsResult = feedItemDAO.getSubscriptionFeedItems(subscription, page, pageSize)
                feedItemPopulator.populateFeedItems(feedItemsResult, mv, "data")
                response.addHeader(xTotalCount, feedItemsResult.totalCount.toString())
                mv
            }
        }

        return forCurrentUser(::renderSubscriptionItems)
    }

    @GetMapping("/subscriptions/{id}/read") // TODO Make a POST
    fun reload(@PathVariable id: String): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withSubscriptionForUser(id, user) { subscription ->
                if (subscription is RssSubscription) {
                    rssPoller.requestRead(subscription)
                    ModelAndView(viewFactory.jsonView()).addObject("data", "ok")
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is not an RSS feed")
                }
            }
        }
    }

    @CrossOrigin
    @GetMapping("/subscriptions/{id}")
    fun subscriptionJson(
        @PathVariable id: String,
        @RequestParam(required = false) page: Int?
    ): ModelAndView {
        fun renderSubscription(user: User): ModelAndView {
            return conditionalLoads.withSubscriptionForUser(id, user) { subscription ->
                ModelAndView(viewFactory.jsonView()).addObject("data", subscription)
            }
        }
        return forCurrentUser(::renderSubscription)
    }

    @PostMapping("/subscriptions")
    fun createSubscription(@RequestBody create: SubscriptionCreateRequest): ModelAndView {
        fun createSubscription(user: User): ModelAndView {
            log.info("Got subscription create request: $create")
            val targetChannel = create.channel
            if (Strings.isNullOrEmpty(create.url) || Strings.isNullOrEmpty(targetChannel)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription url or channel missing")
            }
            return conditionalLoads.withChannelForUser(targetChannel, user) { channel ->
                val subscription = RssSubscription(url = create.url, channelId = channel.id, username = user.username)
                // TODO check for existing idempotent feed
                subscriptionsDAO.add(subscription)
                log.info("Added subscription: $subscription")
                rssPoller.requestRead(subscription)
                ModelAndView(viewFactory.jsonView()).addObject("data", subscription)
            }
        }

        return forCurrentUser(::createSubscription)
    }

    @PutMapping("/subscriptions/{id}")
    fun subscriptionUpdate(
        @PathVariable id: String,
        @RequestBody update: SubscriptionUpdateRequest
    ): ModelAndView {
        fun updateSubscription(user: User): ModelAndView {
            return conditionalLoads.withSubscriptionForUser(id, user) { subscription ->
                log.debug("Got subscription update request: {}", update)
                update.name?.let {
                    subscription.name = update.name
                    subscriptionsDAO.save(subscription)
                    log.info("Updated subscription: $subscription")
                }
                ModelAndView(viewFactory.jsonView()).addObject("data", subscription)
            }
        }
        return forCurrentUser(::updateSubscription)
    }

    @GetMapping("/subscriptions/{id}/delete") // TODO should be a HTTP DELETE
    fun deleteSubscription(@PathVariable id: String): ModelAndView {
        fun performDelete(user: User): ModelAndView {
            return conditionalLoads.withSubscriptionForUser(id, user) { subscription ->
                feedItemDAO.deleteSubscriptionFeedItems(subscription)
                subscriptionsDAO.delete(subscription)
                ModelAndView(viewFactory.jsonView()).addObject("data", "ok")
            }
        }
        return forCurrentUser(::performDelete)
    }

}