package uk.co.eelpieconsulting.feedlistener.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.Subscription
import uk.co.eelpieconsulting.feedlistener.model.User

@Component
class ConditionalLoads @Autowired constructor(private val channelsDAO: ChannelsDAO, val subscriptionsDAO: SubscriptionsDAO) {

    fun withChannelForUser(channelId: String, user: User, handler: (Channel) -> ModelAndView): ModelAndView {
        val channel = channelsDAO.getById(channelId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found")

        if (user.username != channel.username) {    // TODO match by ids
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Channel does not belong to this user")
        }
        return handler(channel)
    }

    fun withSubscriptionForUser(subscriptionId: String, user: User, handler: (Subscription) -> ModelAndView): ModelAndView {
        val subscription = subscriptionsDAO.getById(subscriptionId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")

        if (user.username != subscription.username) {    // TODO match by ids
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription does not belong to this user")
        }
        return handler(subscription)
    }

}
