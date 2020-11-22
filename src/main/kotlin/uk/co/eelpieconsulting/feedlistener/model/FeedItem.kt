package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import dev.morphia.annotations.*
import dev.morphia.utils.IndexType
import org.bson.types.ObjectId
import uk.co.eelpieconsulting.common.geo.model.LatLong
import uk.co.eelpieconsulting.common.views.rss.RssFeedable
import java.util.*

@Entity("feeditems")
@Indexes(Index(fields = [Field(value = "date", type = IndexType.DESC), Field(value = "_id")]), Index(fields = [Field(value = "subscriptionId"), Field(value = "date", type = IndexType.DESC), Field(value = "_id")]))
@JsonInclude(JsonInclude.Include.NON_NULL)
class FeedItem : RssFeedable {

    @Id
    var objectId: ObjectId? = null

    var title: String? = null

    @Indexed
    lateinit var url: String

    var body: String? = null

    private lateinit var date: Date
    override fun getDate(): Date {
        return date
    }

    var place: Place? = null

    private var imageUrl: String? = null

    @Indexed
    lateinit var subscriptionId: String

    @Indexed
    lateinit var channelId: String

    private var author: String? = null
    override fun getAuthor(): String {
        return author.orEmpty()
    }

    // Display only field
    @get:JsonIgnore
    var subscriptionName: String? = null

    constructor() {}
    constructor(title: String?,
                url: String,
                body: String?,
                date: Date,
                place: Place? = null,
                imageUrl: String? = null,
                author: String? = null,
                subscriptionId: String,
                channelId: String) {
        this.title = title
        this.url = url
        this.body = body
        this.date = date
        this.place = place
        this.imageUrl = imageUrl
        this.author = author
        this.subscriptionId = subscriptionId
        this.channelId = channelId
    }

    val id: String?
        get() = url

    @JsonIgnore
    override fun getLatLong(): LatLong? {
        return if (place != null && place!!.latLong != null) {
            LatLong(place!!.latLong.latitude, place!!.latLong.longitude)
        } else null
    }

    fun isGeoTagged(): Boolean {
        return place != null
    }

    // RSS interface methods
    @JsonIgnore
    override fun getDescription(): String? {
        return body
    }

    @JsonIgnore
    override fun getHeadline(): String? {
        return title
    }

    override fun getImageUrl(): String? {
        return imageUrl
    }

    @JsonIgnore
    override fun getWebUrl(): String {
        return url
    }

}