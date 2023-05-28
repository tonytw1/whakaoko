package uk.co.eelpieconsulting.feedlistener.controllers.ui

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.ConditionalLoads
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator
import uk.co.eelpieconsulting.feedlistener.controllers.ui.forms.NewSubscriptionForm
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.rss.RssSubscriptionManager
import uk.co.eelpieconsulting.feedlistener.rss.classification.Classifier

@Controller
class SubscriptionsUIController @Autowired constructor(
    private val subscriptionsDAO: SubscriptionsDAO,
    private val feedItemDAO: FeedItemDAO,
    private val feedItemPopulator: FeedItemPopulator,
    private val rssSubscriptionManager: RssSubscriptionManager,
    private val rssPoller: RssPoller,
    private val urlBuilder: UrlBuilder,
    private val conditionalLoads: ConditionalLoads,
    currentUserService: CurrentUserService,
    request: HttpServletRequest,
    private val classifier: Classifier
) : WithSignedInUser(currentUserService, request) {

    private val log = LogManager.getLogger(SubscriptionsUIController::class.java)

    @GetMapping("/ui/subscriptions/{channelId}/new")
    fun newSubscriptionForm(@PathVariable channelId: String, newSubscriptionForm: NewSubscriptionForm): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(channelId, user) { channel ->
                newSubscriptionPrompt(channel)
            }
        }
    }

    @PostMapping("/ui/subscriptions/feeds")
    fun addFeedSubscription(
        @RequestParam(name = "channel") channelId: String,
        @Valid newSubscriptionForm: NewSubscriptionForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(channelId, user) { channel ->
                if (bindingResult.hasErrors()) {
                    newSubscriptionPrompt(channel)

                } else {
                    val subscription = rssSubscriptionManager.requestFeedSubscription(
                        newSubscriptionForm.url,
                        channel.id,
                        user.username
                    )
                    subscriptionsDAO.add(subscription)
                    log.info("Added subscription: $subscription")
                    rssPoller.requestRead(subscription)

                    redirectAttributes.addFlashAttribute("message", "Subscription added")
                    ModelAndView(RedirectView(urlBuilder.getSubscriptionUrl(subscription)))
                }
            }
        }
    }

    @GetMapping("/ui/subscriptions/{id}")
    fun subscription(@PathVariable id: String, @RequestParam(required = false) page: Int?): ModelAndView {
        return forCurrentUser {
            conditionalLoads.withSubscriptionForUser(id, it) { subscription ->
                conditionalLoads.withChannelForUser(subscription.channelId, it) { channel ->
                    val feedItemsResult = feedItemDAO.getSubscriptionFeedItems(subscription, page)
                    val mv = ModelAndView("subscription")
                    feedItemPopulator.populateFeedItems(feedItemsResult, mv, "feedItems")
                    mv.addObject("user", it)
                        .addObject("channel", channel)
                        .addObject("subscription", subscription)
                        .addObject("frequency", classifier.frequency(subscription))
                    mv
                }
            }
        }
    }

    @GetMapping("/ui/subscriptions/{id}/read")
    fun subscriptionRead(@PathVariable id: String, redirectAttributes: RedirectAttributes): ModelAndView {
        fun executeReload(user: User): ModelAndView {
            return conditionalLoads.withSubscriptionForUser(id, user) { subscription ->
                if (subscription is RssSubscription) {
                    rssPoller.requestRead(subscription)
                    redirectAttributes.addFlashAttribute("message", "Feed is been read")
                    ModelAndView(RedirectView(urlBuilder.getSubscriptionUrl(subscription.id)))
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is not an RSS feed")
                }
            }
        }
        return forCurrentUser(::executeReload)
    }

    private fun newSubscriptionPrompt(
        channel: Channel
    ): ModelAndView =
        ModelAndView("newSubscription").addObject("channel", channel)


}