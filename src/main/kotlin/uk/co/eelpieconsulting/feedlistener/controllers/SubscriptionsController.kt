package uk.co.eelpieconsulting.feedlistener.controllers

import com.google.common.base.Strings
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.annotations.Timed
import uk.co.eelpieconsulting.feedlistener.controllers.ui.WithSignedInUser
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterSubscriptionManager
import javax.servlet.http.HttpServletResponse

@Controller
class SubscriptionsController @Autowired constructor(private val subscriptionsDAO: SubscriptionsDAO,
                                                     private val channelsDAO: ChannelsDAO,
                                                     private val feedItemPopulator: FeedItemPopulator,
                                                     private val feedItemDAO: FeedItemDAO,
                                                     private val viewFactory: ViewFactory,
                                                     private val urlBuilder: UrlBuilder,
                                                     private val rssPoller: RssPoller,
                                                     private val twitterSubscriptionManager: TwitterSubscriptionManager,
                                                     private val twitterListener: TwitterListener,
                                                     currentUserService: CurrentUserService,
                                                     response: HttpServletResponse) : WithSignedInUser(currentUserService, response) {

    private val log = LogManager.getLogger(ChannelsController::class.java)

    private val X_TOTAL_COUNT = "X-Total-Count"

    @Timed(timingNotes = "")
    @GetMapping("/subscriptions/{id}/items")
    fun subscriptionItems(@PathVariable id: String,
                          @RequestParam(required = false) page: Int?,
                          @RequestParam(required = false) format: String?,
                          response: HttpServletResponse): ModelAndView? {

        fun renderSubscriptionItems(user: User): ModelAndView? {
            val subscription = subscriptionsDAO.getById(id)
            if (subscription != null) {
                var mv = ModelAndView(viewFactory.getJsonView())
                if (!Strings.isNullOrEmpty(format) && format == "rss") {
                    val title = if (!Strings.isNullOrEmpty(subscription.name)) subscription.name else subscription.id
                    mv = ModelAndView(viewFactory.getRssView(title, urlBuilder.getSubscriptionUrl(subscription), ""))
                }
                val feedItemsResult = feedItemDAO.getSubscriptionFeedItems(subscription, page)
                feedItemPopulator.populateFeedItems(feedItemsResult, mv, "data")
                response.addHeader(X_TOTAL_COUNT, java.lang.Long.toString(feedItemsResult.totalCount))
                return mv

            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            }
        }

        return forCurrentUser(::renderSubscriptionItems)
    }

    @Timed(timingNotes = "")
    @GetMapping("/subscriptions/{id}/read")
    fun reload(@PathVariable id: String): ModelAndView? {
        fun executeReload(user: User): ModelAndView? {
            val subscription = subscriptionsDAO.getByRssSubscriptionById(id)
            if (subscription != null) {
                log.info("Requesting reload of subscription: " + subscription.name + " / " + subscription.url)
                rssPoller.run(subscription)
                return ModelAndView(viewFactory.getJsonView()).addObject("data", "ok")

            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            }
        }

        return forCurrentUser(::executeReload)
    }

    @Timed(timingNotes = "")
    @GetMapping("/subscriptions/{id}")
    fun subscriptionJson(@PathVariable id: String,
                         @RequestParam(required = false) page: Int?): ModelAndView? {
        fun renderSubscription(user: User): ModelAndView? {
            val subscription = subscriptionsDAO.getById(id)
            if (subscription != null) {
                return ModelAndView(viewFactory.getJsonView()).addObject("data", subscription)
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            }
        }

        return forCurrentUser(::renderSubscription)
    }

    @PostMapping("/subscriptions")
    fun createSubscription(@RequestBody create: SubscriptionCreateRequest): ModelAndView? {
        fun createSubscription(user: User): ModelAndView? {
            log.info("Got subscription create request: " + create)

            if (Strings.isNullOrEmpty(create.url) || Strings.isNullOrEmpty(create.channel)) {
                return null // TODO bad request response
            }

            // TODO check for existing idempotent feed

            val channel = channelsDAO.getById(create.channel)
            if (channel == null) {
                return null // TODO bad request response
            }
            // TODO validate channel user

            val subscription = RssSubscription(url = create.url, channelId = channel.id, username = user.username)
            subscriptionsDAO.add(subscription)
            log.info("Added subscription: $subscription")
            rssPoller.run(subscription)

            return ModelAndView(viewFactory.getJsonView()).addObject("data", subscription)
        }

        return forCurrentUser(::createSubscription)
    }

    @PutMapping("/subscriptions/{id}")
    fun subscriptionUpdate(@PathVariable id: String,
                           @RequestBody update: SubscriptionUpdateRequest): ModelAndView? {
        fun updateSubscription(user: User): ModelAndView? {
            val subscription = subscriptionsDAO.getById(id)
            if (subscription != null) {
                log.debug("Got subscription update request: " + update);
                if (update.name != null) {
                    subscription.name = update.name
                    subscriptionsDAO.save(subscription)
                    log.info("Updated subscription: " + subscription)
                }
                return ModelAndView(viewFactory.getJsonView()).addObject("data", subscription)
            } else {
                return null  // TODO 404
            }
        }

        return forCurrentUser(::updateSubscription)
    }

    @Timed(timingNotes = "")
    @GetMapping("/subscriptions/{id}/delete") // TODO should be a HTTP DELETE
    fun deleteSubscription(@PathVariable username: String, @PathVariable id: String): ModelAndView? {
        fun performDelete(use: User): ModelAndView? {
            val subscription = subscriptionsDAO.getById(id)
            if (subscription == null) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
            }

            when (subscription) {
                is TwitterTagSubscription -> {
                    twitterListener.connect()
                }
            }

            feedItemDAO.deleteSubscriptionFeedItems(subscription)
            subscriptionsDAO.delete(subscription)

            return ModelAndView(viewFactory.getJsonView()).addObject("data", "ok")
        }

        return forCurrentUser(::performDelete)
    }
    
    @Timed(timingNotes = "")
    @PostMapping("/{username}/subscriptions/twitter/tags")
    fun addTwitterTagSubscription(@PathVariable username: String, @RequestParam tag: String, @RequestParam channel: String): ModelAndView? {
        fun executeAddTwitterTag(user: User): ModelAndView? {
            log.info("Twitter tag: $tag")
            twitterSubscriptionManager.requestTagSubscription(tag, channel, username)
            return ModelAndView(RedirectView(urlBuilder.getBaseUrl()))
        }

        return forCurrentUser(::executeAddTwitterTag)
    }

}