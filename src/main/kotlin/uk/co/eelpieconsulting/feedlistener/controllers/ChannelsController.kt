package uk.co.eelpieconsulting.feedlistener.controllers

import com.google.common.base.Strings
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.IdBuilder
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import javax.servlet.http.HttpServletResponse

@Controller
class ChannelsController @Autowired constructor(val usersDAO: UsersDAO, val channelsDAO: ChannelsDAO,
                                                val viewFactory: ViewFactory, val subscriptionsDAO: SubscriptionsDAO,
                                                val urlBuilder: UrlBuilder, val idBuilder: IdBuilder,
                                                val feedItemPopulator: FeedItemPopulator, val feedItemDAO: FeedItemDAO) {

    private val log = Logger.getLogger(ChannelsController::class.java)

    private val X_TOTAL_COUNT = "X-Total-Count"

    @GetMapping("/{username}/channels")
    fun channelsJson(@PathVariable username: String): ModelAndView? {
        usersDAO.getByUsername(username)
        log.info("Channels for user: $username")
        return ModelAndView(viewFactory.getJsonView()).addObject("data", channelsDAO.getChannels(username))
    }

    @GetMapping("/channels/{id}")
    fun channel(@PathVariable username: String, @PathVariable id: String): ModelAndView? {
        usersDAO.getByUsername(username)
        val channel = channelsDAO.getById(id)
        return ModelAndView(viewFactory.getJsonView()).addObject("data", channel)
    }

    @GetMapping("/channels/{id}/subscriptions")
    fun channelSubscriptions(@PathVariable id: String, @RequestParam(required = false) url: String?): ModelAndView? {
        val channel = channelsDAO.getById(id)
        if (channel != null) {
            val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.id, url)
            return ModelAndView(viewFactory.getJsonView()).addObject("data", subscriptionsForChannel)
        } else {
            return null // TODO 404
        }
    }

    @GetMapping("/channels/{id}/items")
    fun channelJson(@PathVariable id: String,
                    @RequestParam(required = false) page: Int?,
                    @RequestParam(required = false) pageSize: Int?,
                    @RequestParam(required = false) format: String?,
                    @RequestParam(required = false) q: String?,
                    response: HttpServletResponse): ModelAndView? {
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
            return null // TODO 404
        }
    }

    @RequestMapping("/{username}/channels")
    fun addChannel(@PathVariable username: String, @RequestParam name: String): ModelAndView? {
        usersDAO.getByUsername(username)
        val newChannel = Channel(idBuilder.makeIdFor(name), name, username)
        channelsDAO.add(username, newChannel)
        return ModelAndView(RedirectView(urlBuilder.getChannelUrl(newChannel)))
    }

    @RequestMapping("/backfill")
    fun backfill(): ModelAndView? {
        var watermark = DateTime.now().plusMonths(6).toDate()
        var feedItems = feedItemDAO.before(watermark)
        while (feedItems != null && feedItems.isNotEmpty()) {
            log.info("Backfilling feeditems before: " + watermark)
            feedItems.forEach { i ->
                val subscriptionId = i.subscriptionId;
                val byId = subscriptionsDAO.getById(subscriptionId);
                if (byId != null) {
                    val channelId = byId.channelId
                    i.channelId = channelId;
                    feedItemDAO.update(i)
                } else {
                    log.warn("Uknown subscription: " + i.subscriptionId)
                }
            }
            watermark = feedItems.last().date
            feedItems = feedItemDAO.before(watermark)
        }

        log.info("Backfill finished")
        return null
    }

}