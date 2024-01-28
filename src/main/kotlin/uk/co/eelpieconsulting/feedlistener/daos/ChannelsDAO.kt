package uk.co.eelpieconsulting.feedlistener.daos

import com.mongodb.MongoException
import com.mongodb.MongoWriteException
import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.filters.Filters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User

@Component
class ChannelsDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory) {

    private val nameAscending = Sort.ascending("name")

    fun getChannelsFor(user: User): List<Channel> {
        return try {
            val channelsByUser = queryForUsersChannels(user)
            channelsByUser.iterator(FindOptions().sort(nameAscending)).toList()
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getById(id: String): Channel? {
        return dataStoreFactory.get().find(Channel::class.java).filter(Filters.eq("id", id)).first()
    }

    fun usersChannelByName(user: User, name: String): Channel? {
        val byUserAndName = queryForUsersChannels(user).filter(Filters.eq("name", name))
        return byUserAndName.first()
    }

    fun save(channel: Channel): Boolean {
        return try {
            dataStoreFactory.get().save(channel)
            true
        } catch (e: MongoWriteException) {
            false
        }
    }

    private fun queryForUsersChannels(user: User) =
        dataStoreFactory.get().find(Channel::class.java).filter(Filters.eq("username", user.username))
    
}