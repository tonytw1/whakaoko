package uk.co.eelpieconsulting.feedlistener.daos

import com.mongodb.MongoException
import dev.morphia.query.experimental.filters.Filters
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.Channel

@Component
class ChannelsDAO @Autowired constructor(val dataStoreFactory: DataStoreFactory) {

    private val log = Logger.getLogger(ChannelsDAO::class.java)

    fun getChannels(username: String?): List<Channel> {
        return try {
            val channelsByUser = dataStoreFactory.getDs().find<Channel>(Channel::class.java).filter(Filters.eq("username", username))
            channelsByUser.iterator().toList()
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getById(username: String?, id: String?): Channel {
        return try {
            dataStoreFactory.getDs().find<Channel>(Channel::class.java).filter(Filters.eq("id", id), Filters.eq("username", username)).first()
        } catch (e: MongoException) {
            throw RuntimeException(e)   // TODO
        }
    }

    @Synchronized
    fun add(username: String, channel: Channel) {
        if (!channelExists(username, channel)) {
            log.info("Adding new channel: $channel")
            save(channel)
        } else {
            log.warn("Ignoring existing channel: $channel")
        }
    }

    fun save(channel: Channel?) {
        try {
            dataStoreFactory.getDs().save<Channel>(channel)
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    private fun channelExists(username: String, channel: Channel): Boolean {
        for (existingChannel in getChannels(username)) {
            if (existingChannel.id == channel.id) {
                return true
            }
        }
        return false
    }
    
}