package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import org.bson.types.ObjectId

@Entity("users")
class User {
    @Id
    var objectId: ObjectId? = null
    var username: String? = null

    @get:JsonIgnore
    var password: String? = null
    var googleUserId: String? = null

    @get:JsonIgnore
    var accessToken: String? = null

    constructor()
    constructor(username: String?, password: String?) {
        this.username = username
        this.password = password
    }

    override fun toString(): String {
        return "User{" +
                "objectId=" + objectId +
                ", username='" + username + '\'' +
                ", googleUserId='" + googleUserId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}'
    }
}
