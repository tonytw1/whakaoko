package uk.co.eelpieconsulting.feedlistener.daos

import dev.morphia.query.FindOptions
import dev.morphia.query.Sort
import dev.morphia.query.experimental.filters.Filters
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.User

@Component
class UsersDAO @Autowired constructor(private val dataStoreFactory: DataStoreFactory){

    private val USERNAME_ASCENDING = Sort.ascending("username")

    fun getUsers(): List<User?>? {
        return dataStoreFactory.get().find(User::class.java).iterator(FindOptions().sort(USERNAME_ASCENDING)).toList()
    }

    fun getByObjectId(objectId: String): User? {
        val oid = ObjectId(objectId)
        return dataStoreFactory.get().find(User::class.java).filter(Filters.eq("_id", oid)).first()
    }

    fun getByUsername(username: String): User? {
        return dataStoreFactory.get().find(User::class.java).filter(Filters.eq("username", username)).first()
    }

    fun getByAccessToken(token: String): User? {
        return dataStoreFactory.get().find(User::class.java).filter(Filters.eq("accessToken", token)).first()
    }

    fun getByGoogleId(googleId: String): User? {
         return dataStoreFactory.get().find(User::class.java).filter(Filters.eq("googleUserId", googleId)).first()
    }

    fun save(user: User) {
        dataStoreFactory.get().save(user)
    }

}