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
            val channelsByUser = queryForUsersChannels(user)
            channelsByUser.iterator(FindOptions().sort(NAME_ASCENDING)).toList()
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getById(id: String): Channel? {
        return dataStoreFactory.get().find<Channel>(Channel::class.java).filter(Filters.eq("id", id)).first()
    }

    fun usersChannelByName(user: User, name: String): Channel? {
        val byUserAndName = queryForUsersChannels(user).filter(Filters.eq("name", name))
        return byUserAndName.first()
    }

    fun add(channel: Channel) {
        log.info("Adding new channel: $channel")
        save(channel)
    }
    fun save(channel: Channel) {   // TODO why 2?
        try {
            dataStoreFactory.get().save<Channel>(channel)
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    private fun queryForUsersChannels(user: User) =
        dataStoreFactory.get().find<Channel>(Channel::class.java).filter(Filters.eq("username", user.username))
    
}