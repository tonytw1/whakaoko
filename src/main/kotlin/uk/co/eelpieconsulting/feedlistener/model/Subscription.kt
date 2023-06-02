package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import dev.morphia.annotations.Indexed
import org.bson.types.ObjectId
import uk.co.eelpieconsulting.feedlistener.rss.classification.FeedStatus
import java.util.*

@Entity("subscriptions")
@JsonPropertyOrder("id", "name", "channel", "url")
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class Subscription {
    @Id
    var objectId: ObjectId? = null

    lateinit var id: String
    var name: String? = null
    lateinit var username: String
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    var lastRead: Date? = null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    var latestItemDate: Date? = null
    var error: String? = null
    var etag: String? = null
    var httpStatus: Int? = null
    var itemCount= 0L
    var lastModified: Date? = null
    var classifications: Set<FeedStatus>? = emptySet()

    @Indexed
    lateinit var channelId: String

}