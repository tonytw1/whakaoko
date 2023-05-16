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
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.ui.WithSignedInUser
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller

@Controller
class SubscriptionsController @Autowired constructor(private val subscriptionsDAO: SubscriptionsDAO,
                                                     private val channelsDAO: ChannelsDAO,
                                                     private val feedItemPopulator: FeedItemPopulator,
                                                     private val feedItemDAO: FeedItemDAO,
                                                     private val viewFactory: ViewFactory,
                                                     private val urlBuilder: UrlBuilder,
                                                     private val rssPoller: RssPoller,
                                                     private val conditionalLoads: ConditionalLoads,
                                                     currentUserService: CurrentUserService,
                                                     request: HttpServletRequest) : WithSignedInUser(currentUserService, request) {

    private val log = LogManager.getLogger(ChannelsController::class.java)

    private val X_TOTAL_COUNT = "X-Total-Count"

    @CrossOrigin
    @GetMapping("/subscriptions/{id}/items")
    fun subscriptionItems(@PathVariable id: String,
                          @RequestParam(required = false) page: Int?,
                          @RequestParam(required = false) pageSize: Int?,
                          @RequestParam(required = false) format: String?,
                          response: HttpServletResponse): ModelAndView {

        fun renderSubscriptionItems(user: User): ModelAndView {
            val subscription = subscriptionsDAO.getById(id)
            if (subscription != null) {
                var mv = ModelAndView(viewFactory.jsonView)
                if (!Strings.isNullOrEmpty(format) && format == "rss") {
                    val title = if (!Strings.isNullOrEmpty(subscription.name)) subscription.name else subscription.id
                    mv = ModelAndView(viewFactory.getRssView(title, urlBuilder.getSubscriptionUrl(subscription), ""))
                }
                val feedItemsResult = feedItemDAO.getSubscriptionFeedItems(subscription, page, pageSize)
                feedItemPopulator.populateFeedItems(feedItemsResult, mv, "data")
                response.addHeader(X_TOTAL_COUNT, feedItemsResult.totalCount.toString())
                return mv

            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
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
                    ModelAndView(viewFactory.jsonView).addObject("data", "ok")
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is not an RSS feed")
                }
            }
        }
    }

    @CrossOrigin
    @GetMapping("/subscriptions/{id}")
    fun subscriptionJson(@PathVariable id: String,
                         @RequestParam(required = false) page: Int?): ModelAndView {
        fun renderSubscription(user: User): ModelAndView {
            val subscription = subscriptionsDAO.getById(id)
            if (subscription != null) {
                return ModelAndView(viewFactory.jsonView).addObject("data", subscription)
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            }
        }

        return forCurrentUser(::renderSubscription)
    }

    @PostMapping("/subscriptions")
    fun createSubscription(@RequestBody create: SubscriptionCreateRequest): ModelAndView {
        fun createSubscription(user: User): ModelAndView {
            log.info("Got subscription create request: $create")

            if (Strings.isNullOrEmpty(create.url) || Strings.isNullOrEmpty(create.channel)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription url or channel missing")
            }

            // TODO check for existing idempotent feed

            val channel = channelsDAO.getById(create.channel)
            if (channel == null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Channel not found")
            }
            // TODO validate channel user

            val subscription = RssSubscription(url = create.url, channelId = channel.id, username = user.username)
            subscriptionsDAO.add(subscription)
            log.info("Added subscription: $subscription")
            rssPoller.requestRead(subscription)

            return ModelAndView(viewFactory.jsonView).addObject("data", subscription)
        }

        return forCurrentUser(::createSubscription)
    }

    @PutMapping("/subscriptions/{id}")
    fun subscriptionUpdate(@PathVariable id: String,
                           @RequestBody update: SubscriptionUpdateRequest): ModelAndView {
        fun updateSubscription(user: User): ModelAndView {
            val subscription = subscriptionsDAO.getById(id)
            if (subscription != null) {
                log.debug("Got subscription update request: {}", update)
                if (update.name != null) {
                    subscription.name = update.name
                    subscriptionsDAO.save(subscription)
                    log.info("Updated subscription: $subscription")
                }
                return ModelAndView(viewFactory.jsonView).addObject("data", subscription)
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            }
        }

        return forCurrentUser(::updateSubscription)
    }

    @GetMapping("/subscriptions/{id}/delete") // TODO should be a HTTP DELETE
    fun deleteSubscription(@PathVariable id: String): ModelAndView {
        fun performDelete(user: User): ModelAndView {
            val subscription = subscriptionsDAO.getById(id)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            feedItemDAO.deleteSubscriptionFeedItems(subscription)
            subscriptionsDAO.delete(subscription)

            return ModelAndView(viewFactory.jsonView).addObject("data", "ok")
        }

        return forCurrentUser(::performDelete)
    }

}