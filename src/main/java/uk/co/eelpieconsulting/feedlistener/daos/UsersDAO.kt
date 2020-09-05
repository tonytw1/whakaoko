package uk.co.eelpieconsulting.feedlistener.daos

import com.google.common.base.Strings
import com.mongodb.MongoException
import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException
import uk.co.eelpieconsulting.feedlistener.model.User

@Component
class UsersDAO @Autowired constructor(val dataStoreFactory: DataStoreFactory){

    private val USERNAME_ASCENDING = Sort.ascending("username")

    fun getUsers(): List<User?>? {
        return try {
            dataStoreFactory.getDs().find(User::class.java).iterator(FindOptions().sort(this.USERNAME_ASCENDING)).toList()
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

    fun getByUsername(username: String): User? {
        val user = loadUserFromDatabase(username)
        if (user != null) {
            return user
        }
        throw UnknownUserException()
    }

    @Synchronized
    fun createUser(username: String) {
        if (Strings.isNullOrEmpty(username)) {
            throw RuntimeException("No username given")
        }
        if (loadUserFromDatabase(username) != null) {
            throw RuntimeException("Username is not available")
        }
        save(User(username))
    }

    fun save(user: User?) {
        dataStoreFactory.getDs().save(user)
    }

    private fun loadUserFromDatabase(username: String): User? {
        return try {
            dataStoreFactory.getDs().find(User::class.java).filter(Filters.eq("username", username)).first()
        } catch (e: MongoException) {
            throw RuntimeException(e)
        }
    }

}