package uk.co.eelpieconsulting.feedlistener.daos

import com.mongodb.MongoException
import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.apache.logging.log4j.LogManager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User

@Component
class ChannelsDAO @Autowired constructor(val dataStoreFactory: DataStoreFactory) {

    private val log = LogManager.getLogger(ChannelsDAO::class.java)

    private val NAME_ASCENDING = Sort.ascending("name")

    fun getChannelsFor(user: User): List<Channel> {
        return try {
            val channelsByUser = dataStoreFactory.get().find<Channel>(Channel::class.java).filter(Filters.eq("username", user.username))
            channelsByUser.iterator(FindOptions().sort(NAME_ASCENDING)).toList()
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getById(id: String): Channel? {
        return dataStoreFactory.get().find<Channel>(Channel::class.java).filter(Filters.eq("id", id)).first()
    }

    fun usersChannelByName(user: User, name: String): Channel? {
        return getChannelsFor(user).find { it.name == name }   // TODO proper looking; this will break with pagination
    }

    @Synchronized
    fun add(user: User, channel: Channel) {
        if (!channelExists(user, channel)) {
            log.info("Adding new channel: $channel")
            save(channel)
        } else {
            log.warn("Ignoring existing channel: $channel")
        }
    }

    fun save(channel: Channel?) {
        try {
            dataStoreFactory.get().save<Channel>(channel)
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    private fun channelExists(user: User, channel: Channel): Boolean {
        for (existingChannel in getChannelsFor(user)) {
            if (existingChannel.id == channel.id) {
                return true
            }
        }
        return false
    }
    
}