package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import dev.morphia.annotations.*
import dev.morphia.utils.IndexType
import org.bson.types.ObjectId
import uk.co.eelpieconsulting.common.geo.model.LatLong
import uk.co.eelpieconsulting.common.views.rss.RssFeedable
import java.util.*

@Entity("feeditems")
@Indexes(
    Index(fields = [Field(value = "date", type = IndexType.DESC), Field(value = "_id")]),
    Index(fields = [Field(value = "ordering", type = IndexType.DESC), Field(value = "_id")]),
    Index(fields = [Field(value = "subscriptionId"), Field(value = "date", type = IndexType.DESC), Field(value = "_id")]),
    Index(fields = [Field(value = "subscriptionId"), Field(value = "ordering", type = IndexType.DESC), Field(value = "_id")])
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class FeedItem : RssFeedable {

    @Id
    var objectId: ObjectId? = null

    var title: String? = null

    @Indexed
    lateinit var url: String

    var body: String? = null

    private var date: Date? = null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    override fun getDate(): Date? {
        return date
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    var accepted: Date? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    var ordering: Date? = null

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

    @JsonIgnore
    override fun getCategories(): MutableList<String> {
        val categories: List<Category> = _categories ?: emptyList<Category>()
        return categories.mapNotNull { it.value }.toMutableList()
    }

    // Display only field
    @get:JsonIgnore
    var subscriptionName: String? = null

    @JsonProperty("categories")
    @Property("categories") // TODO clear this clash by renaming rss categories interface method
    var _categories: List<Category>? = null

    constructor() {}
    constructor(title: String?,
                url: String,
                body: String?,
                date: Date?,
                accepted: Date? = null,
                place: Place? = null,
                imageUrl: String? = null,
                author: String? = null,
                subscriptionId: String,
                channelId: String,
                categories: List<Category>?,
                ordering: Date?
    ) {
        this.title = title
        this.url = url
        this.body = body
        this.date = date
        this.accepted = accepted
        this.place = place
        this.imageUrl = imageUrl
        this.author = author
        this.subscriptionId = subscriptionId
        this.channelId = channelId
        this._categories = categories
        this.ordering = ordering
    }

    val id: String?
        get() = url

    @JsonIgnore
    override fun getLatLong(): LatLong? {
        val latLong = place?.latLong
        return if (latLong != null) {
            LatLong(latLong.latitude, latLong.longitude)
        } else null
    }

    override fun getFeatureName(): String? {
       return place?.address
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

    fun copy(): FeedItem {
        return FeedItem(
            this.title,
            this.url,
            this.body,
            this.date,
            this.accepted,
            this.place,
            this.imageUrl,
            this.author,
            this.subscriptionId,
            this.channelId,
            this._categories,
            this.ordering
        )
    }

    override fun toString(): String {
        return "FeedItem(objectId=$objectId, title=$title, url='$url', body=$body, date=$date, accepted=$accepted, place=$place, imageUrl=$imageUrl, subscriptionId='$subscriptionId', channelId='$channelId', author=$author, _categories=$_categories)"
    }

}