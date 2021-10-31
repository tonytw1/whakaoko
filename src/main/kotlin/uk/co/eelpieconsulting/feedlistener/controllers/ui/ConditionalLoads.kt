package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User

@Component
class ConditionalLoads @Autowired constructor(val channelsDAO: ChannelsDAO) {

    fun withChannelForUser(channelId: String, user: User, handler: (Channel) -> ModelAndView): ModelAndView {
        val channel: Channel? = channelsDAO.getById(channelId)
        if (channel == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found")
        }
        if (user.username != channel.username) {    // TODO match by ids
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Channel does not belong to this user")
        }
        return handler(channel)
    }

}
