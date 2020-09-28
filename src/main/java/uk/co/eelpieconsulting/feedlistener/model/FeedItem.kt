package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.morphia.annotations.*
import dev.morphia.utils.IndexDirection
import dev.morphia.utils.IndexType
import org.bson.types.ObjectId
import uk.co.eelpieconsulting.common.geo.model.LatLong
import uk.co.eelpieconsulting.common.views.rss.RssFeedable
import java.io.Serializable
import java.util.*

@Entity("feeditems")
@Indexes(Index(fields = [Field(value = "date", type = IndexType.DESC), Field(value = "_id")]), Index(fields = [Field(value = "subscriptionId"), Field(value = "date", type = IndexType.DESC), Field(value = "_id")]))
class FeedItem : Serializable, RssFeedable {

    @Id
    var objectId: ObjectId? = null

    var title: String? = null
        private set

    @Indexed
    var url: String? = null

    var body: String? = null
        private set

    @Indexed(value = IndexDirection.DESC)
    private lateinit var date: Date      // TODO unused - because always used with subscription id?
    override fun getDate(): Date {
        return date
    }

    var place: Place? = null
        private set
    private var imageUrl: String? = null

    @Indexed
    var subscriptionId: String? = null

    @Indexed
    var channelId: String? = null
    private var author: String? = null

    // Display only field
    @get:JsonIgnore
    var subscriptionName: String? = null

    constructor() {}
    constructor(title: String?, url: String?, body: String?,
                date: Date, place: Place?,
                imageUrl: String?, author: String?,
                subscriptionId: String?,
                channelId: String?) {
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

    override fun getImageUrl(): String? {
        return imageUrl
    }

    val isGeoTagged: Boolean
        get() = place != null

    override fun getAuthor(): String? {
        return author
    }

    @JsonIgnore
    override fun getDescription(): String? {
        return body
    }

    @JsonIgnore
    override fun getHeadline(): String? {
        return title
    }

    @JsonIgnore
    override fun getWebUrl(): String {
        return url!!
    }

}