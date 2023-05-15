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
data class FeedItem(
    val title: String?,
    @Indexed
    val url: String,
    val body: String?,
    private var date: Date?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") val accepted: Date? = null,
    val place: Place? = null,
    private val imageUrl: String? = null,
    private val author: String? = null,
    @Indexed
    val subscriptionId: String,
    @Indexed
    val channelId: String,
    @JsonProperty("categories") @Property("categories") val _categories: List<Category>?,   // TODO clear this clash by renaming rss categories interface method
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") val ordering: Date?,
    @get:JsonIgnore val subscriptionName: String? = null // Display only field
) : RssFeedable {

    @Id
    var objectId: ObjectId? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    override fun getDate(): Date? {
        return date
    }

    override fun getAuthor(): String {
        return author.orEmpty()
    }

    @JsonIgnore
    override fun getCategories(): MutableList<String> {
        val categories: List<Category> = _categories ?: emptyList<Category>()
        return categories.mapNotNull { it.value }.toMutableList()
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

}