package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
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
                                                       response: HttpServletResponse) : WithSignedInUser(currentUserService, response) {

    private val log = LogManager.getLogger(SubscriptionsUIController::class.java)

    @GetMapping("/ui/subscriptions/new")
    fun newSubscriptionForm(): ModelAndView? {
        fun newChannelPrompt(user: User): ModelAndView {
            return ModelAndView("newSubscription").
            addObject("username", user.username).
            addObject("channels", channelsDAO.getChannels(user.username))
        }

        return forCurrentUser(::newChannelPrompt)
    }

    @PostMapping("/ui/subscriptions/feeds")
    fun addFeedSubscription(@RequestParam url: String, @RequestParam channel: String): ModelAndView? {
        fun executeAddSubscription(user: User): ModelAndView? {
            // TODO form binding and validation
            val subscription = rssSubscriptionManager.requestFeedSubscription(url, channel, user.username)
            subscriptionsDAO.add(subscription)
            log.info("Added subscription: $subscription")
            rssPoller.run(subscription)
            return ModelAndView(RedirectView(urlBuilder.getSubscriptionUrl(subscription.id)))
        }
        return forCurrentUser(::executeAddSubscription)
    }

    @GetMapping("/ui/subscriptions/{id}")
    fun subscription(@PathVariable id: String?, @RequestParam(required = false) page: Int?): ModelAndView? {
        fun meh(user: User): ModelAndView? {
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

        return forCurrentUser(::meh)
    }

}