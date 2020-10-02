package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import dev.morphia.annotations.Indexed
import org.bson.types.ObjectId
import java.util.*

@Entity("subscriptions")
@JsonPropertyOrder("id", "name", "channel", "url")
abstract class Subscription {
    @Id
    var objectId: ObjectId? = null

    lateinit var id: String
    var name: String? = null
    lateinit var username: String
    var lastRead: Date? = null
    var latestItemDate: Date? = null
    var error: String? = null
    var etag: String? = null
    var httpStatus: Int? = null
    var itemCount= 0L

    @Indexed
    lateinit var channelId: String

}