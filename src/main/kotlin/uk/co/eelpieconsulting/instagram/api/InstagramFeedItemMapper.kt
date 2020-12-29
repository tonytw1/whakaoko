package uk.co.eelpieconsulting.instagram.api

import org.apache.logging.log4j.LogManager
import org.joda.time.DateTime
import org.json.JSONException
import org.json.JSONObject
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.LatLong
import uk.co.eelpieconsulting.feedlistener.model.Place
import java.util.*

class InstagramFeedItemMapper {

    private val log = LogManager.getLogger(InstagramFeedItemMapper::class.java)

    private val USERNAME = "username"
    private val USER = "user"
    private val LATITUDE = "latitude"
    private val LONGITUDE = "longitude"
    private val LOCATION = "location"
    private val LINK = "link"
    private val CREATED_TIME = "created_time"
    private val TEXT = "text"
    private val URL = "url"
    private val STANDARD_RESOLUTION = "standard_resolution"
    private val CAPTION = "caption"
    private val IMAGES = "images"

    @Throws(JSONException::class)
    fun createFeedItemFrom(json: JSONObject): FeedItem {
        var imageUrl: String? = null
        if (json.has(IMAGES)) {
            val imagesJson = json.getJSONObject(IMAGES)
            imageUrl = imagesJson.getJSONObject(STANDARD_RESOLUTION).getString(URL)
        }
        var caption: String? = null
        if (json.has(CAPTION) && !json.isNull(CAPTION)) {
            val captionJson = json.getJSONObject(CAPTION)
            caption = captionJson.getString(TEXT)
        }
        val createdTime = DateTime(json.getLong(CREATED_TIME) * 1000)
        val url = json.getString(LINK)
        var place: Place? = null
        if (json.has(LOCATION) && !json.isNull(LOCATION)) {
            val locationJson = json.getJSONObject(LOCATION)
            if (locationJson.has(LATITUDE) && locationJson.has(LONGITUDE)) {
                val latLong = LatLong(locationJson.getDouble(LATITUDE), locationJson.getDouble(LONGITUDE)) // TODO preserve name and id if available.
                place = Place(null, latLong)
            } else {
                log.warn("Location has no lat long: $locationJson")
            }
        }
        var author: String? = null
        if (json.has(USER)) {
            val userJson = json.getJSONObject(USER)
            if (userJson.has(USERNAME)) {
                author = userJson.getString(USERNAME)
            }
        }
        return FeedItem(caption, url, null, createdTime.toDate(), place, imageUrl, author, UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

}