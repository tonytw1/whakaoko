package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import org.bson.types.ObjectId

@Entity("users")
data class User(
    @Id val objectId: ObjectId,
    val username: String,
    @get:JsonIgnore val password: String? = null,
    @get:JsonIgnore var googleUserId: String? = null,
    @get:JsonIgnore var accessToken: String? = null
)
