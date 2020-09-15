package uk.co.eelpieconsulting.feedlistener.controllers

import com.google.common.base.Strings
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.common.geo.model.LatLong
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.annotations.Timed
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscriptionManager
import uk.co.eelpieconsulting.feedlistener.model.*
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.rss.RssSubscriptionManager
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterSubscriptionManager
import javax.servlet.http.HttpServletResponse

@Controller
class SubscriptionsController @Autowired constructor(val usersDAO: UsersDAO, val subscriptionsDAO: SubscriptionsDAO,
                                                     val feedItemPopulator: FeedItemPopulator, val feedItemDAO: FeedItemDAO, val viewFactory: ViewFactory,
                                                     val urlBuilder: UrlBuilder, val rssSubscriptionManager: RssSubscriptionManager,
                                                     val rssPoller: RssPoller,
                                                     val twitterSubscriptionManager: TwitterSubscriptionManager,
                                                     val instagramSubscriptionManager: InstagramSubscriptionManager, val twitterListener: TwitterListener) {

    private val log = Logger.getLogger(ChannelsController::class.java)

    private val X_TOTAL_COUNT = "X-Total-Count"

    @Timed(timingNotes = "")
    @GetMapping("/{username}/subscriptions/{id}/items")
    fun subscriptionItems(@PathVariable username: String, @PathVariable id: String,
                          @RequestParam(required = false) page: Int?,
                          @RequestParam(required = false) format: String?,
                          response: HttpServletResponse): ModelAndView? {
        usersDAO.getByUsername(username)
        val subscription = subscriptionsDAO.getById(id)
        var mv = ModelAndView(viewFactory.getJsonView())
        if (!Strings.isNullOrEmpty(format) && format == "rss") {
            val title = if (!Strings.isNullOrEmpty(subscription.name)) subscription.name else subscription.id
            mv = ModelAndView(viewFactory.getRssView(title, urlBuilder.getSubscriptionUrl(subscription), ""))
        }
        val feedItemsResult = feedItemDAO.getSubscriptionFeedItems(subscription, page)
        feedItemPopulator.populateFeedItems(feedItemsResult, mv, "data")
        response.addHeader(X_TOTAL_COUNT, java.lang.Long.toString(feedItemsResult.totalCount))
        return mv
    }

    @Timed(timingNotes = "")
    @GetMapping("/{username}/subscriptions/{id}/read")
    fun reload(@PathVariable username: String, @PathVariable id: String): ModelAndView? {
        usersDAO.getByUsername(username)
        val subscription = subscriptionsDAO.getById(id) as RssSubscription
        log.info("Requesting reload of subscription: " + subscription.name + " / " + subscription.url)
        rssPoller.run(subscription)
        return ModelAndView(viewFactory.getJsonView()).addObject("data", "ok")
    }

    @Timed(timingNotes = "")
    @GetMapping("/{username}/subscriptions/{id}")
    fun subscriptionJson(@PathVariable username: String, @PathVariable id: String,
                         @RequestParam(required = false) page: Int?): ModelAndView? {
        usersDAO.getByUsername(username)
        val subscription = subscriptionsDAO.getById(id)
        return ModelAndView(viewFactory.getJsonView()).addObject("data", subscription)
    }

    @Timed(timingNotes = "")
    @GetMapping("/{username}/subscriptions/{id}/delete") // TODO should be a HTTP DELETE
    fun deleteSubscription(@PathVariable username: String, @PathVariable id: String): ModelAndView? {
        usersDAO.getByUsername(username)
        val subscription = subscriptionsDAO.getById(id)
                ?: // TODO 404
                return null

        feedItemDAO.deleteSubscriptionFeedItems(subscription)
        subscriptionsDAO.delete(subscription)
        if (subscription.id.startsWith("twitter")) {
            twitterListener.connect()
        }
        if (subscription.id.startsWith("instagram")) {
            instagramSubscriptionManager.requestUnsubscribeFrom((subscription as InstagramSubscription).subscriptionId)
        }
        return ModelAndView(viewFactory.getJsonView()).addObject("data", "ok")
    }

    @Timed(timingNotes = "")
    @RequestMapping(value = ["/{username}/subscriptions"], method = [RequestMethod.GET])
    fun subscriptions(@PathVariable username: String, @RequestParam(required = false) url: String?): ModelAndView? {
        usersDAO.getByUsername(username)
        return ModelAndView(viewFactory.getJsonView()).
        addObject("data", subscriptionsDAO.getSubscriptions(SubscriptionsDAO.LATEST_ITEM_DATE_DESCENDING, url))
    }

    @Timed(timingNotes = "")
    @GetMapping("/subscriptions")
    fun all(): ModelAndView? {
        return try {
            ModelAndView(viewFactory.getJsonView()).addObject("data", subscriptionsDAO.allSubscriptions())
        } catch (e: Exception) {
            log.error(e)
            throw e
        }
    }

    @Timed(timingNotes = "")
    @PostMapping("/{username}/subscriptions/feeds")
    fun addFeedSubscription(@PathVariable username: String, @RequestParam url: String, @RequestParam channel: String): ModelAndView? {
        usersDAO.getByUsername(username)
        val subscription = rssSubscriptionManager.requestFeedSubscription(url, channel, username)
        subscriptionsDAO.add(subscription)
        log.info("Added subscription: $subscription")
        rssPoller.run(subscription)
        return ModelAndView(viewFactory.getJsonView()).addObject("data", subscription)
    }

    @Timed(timingNotes = "")
    @PostMapping("/{username}/subscriptions/twitter/tags")
    fun addTwitterTagSubscription(@PathVariable username: String, @RequestParam tag: String, @RequestParam channel: String): ModelAndView? {
        log.info("Twitter tag: $tag")
        twitterSubscriptionManager.requestTagSubscription(tag, channel, username)
        return ModelAndView(RedirectView(urlBuilder.getBaseUrl()))
    }

    @Timed(timingNotes = "")
    @PostMapping("/{username}/subscriptions/instagram/tags")
    fun addInstagramTagSubscription(@PathVariable username: String,
                                    @RequestParam tag: String, @RequestParam channel: String): ModelAndView? {
        log.info("Instagram tag")
        val subscription = instagramSubscriptionManager.requestInstagramTagSubscription(tag, channel, username)
        subscriptionsDAO.add(subscription)
        return ModelAndView(RedirectView(urlBuilder.getBaseUrl()))
    }

    @Timed(timingNotes = "")
    @PostMapping("/{username}/subscriptions/instagram/geography")
    fun addInstagramTagSubscription(@PathVariable username: String,
                                    @RequestParam latitude: Double,
                                    @RequestParam longitude: Double,
                                    @RequestParam radius: Int,
                                    @RequestParam channel: String): ModelAndView? {
        val latLong = LatLong(latitude, longitude)
        val instagramGeographySubscription = instagramSubscriptionManager.requestInstagramGeographySubscription(latLong, radius, channel, username)
        log.info("Saving subscription: $instagramGeographySubscription")
        subscriptionsDAO.add(instagramGeographySubscription)
        return ModelAndView(RedirectView(urlBuilder.getBaseUrl()))
    }

}