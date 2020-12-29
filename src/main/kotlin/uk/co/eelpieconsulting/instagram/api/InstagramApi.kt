package uk.co.eelpieconsulting.instagram.api

import com.google.common.collect.Lists
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import org.json.JSONException
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.LatLong
import uk.co.eelpieconsulting.common.http.HttpBadRequestException
import uk.co.eelpieconsulting.common.http.HttpFetchException
import uk.co.eelpieconsulting.common.http.HttpFetcher
import uk.co.eelpieconsulting.feedlistener.model.FeedItem
import uk.co.eelpieconsulting.feedlistener.model.InstagramGeographySubscription
import uk.co.eelpieconsulting.feedlistener.model.InstagramTagSubscription
import java.io.UnsupportedEncodingException
import java.util.*

@Component
class InstagramApi @Autowired constructor(val commonHttpFetcher: HttpFetcher) {

    private val log = LogManager.getLogger(InstagramApi::class.java)
    private val INSTAGRAM_API_AUTHORIZE = "https://api.instagram.com/oauth/authorize/"
    private val INSTAGRAM_API_ACCESS_TOKEN = "https://api.instagram.com/oauth/access_token"
    private val INSTAGRAM_API_V1_SUBSCRIPTIONS = "https://api.instagram.com/v1/subscriptions"
    private val DATA = "data"

    private val mapper = InstagramFeedItemMapper()

    @Throws(HttpFetchException::class, UnsupportedEncodingException::class, JSONException::class)
    fun createTagSubscription(tag: String?, clientId: String, clientSecret: String, callbackUrl: String, channelId: String?, username: String?): InstagramTagSubscription {
        val nameValuePairs = commonSubscriptionFields(clientId, clientSecret, callbackUrl)
        nameValuePairs.add(BasicNameValuePair("object", "tag"))
        nameValuePairs.add(BasicNameValuePair("object_id", tag))
        val post = HttpPost(INSTAGRAM_API_V1_SUBSCRIPTIONS)
        val entity: HttpEntity = UrlEncodedFormEntity(nameValuePairs)
        post.entity = entity
        return try {
            val response = commonHttpFetcher.post(post)
            val responseJSON = JSONObject(response)
            InstagramTagSubscription(
                    responseJSON.getJSONObject(DATA).getString("object_id"),
                    responseJSON.getJSONObject(DATA).getLong("id"), channelId, username)
        } catch (e: HttpBadRequestException) {
            log.error("HTTP Bad request: " + e.responseBody)
            throw RuntimeException(e)
        }
    }

    @Throws(UnsupportedEncodingException::class, HttpFetchException::class, JSONException::class)
    fun createGeographySubscription(latLong: LatLong, radius: Int, clientId: String, clientSecret: String, callbackUrl: String, channelId: String?, username: String?): InstagramGeographySubscription {
        val nameValuePairs = commonSubscriptionFields(clientId, clientSecret, callbackUrl)
        nameValuePairs.add(BasicNameValuePair("object", "geography"))
        nameValuePairs.add(BasicNameValuePair("lat", java.lang.Double.toString(latLong.latitude)))
        nameValuePairs.add(BasicNameValuePair("lng", java.lang.Double.toString(latLong.longitude)))
        nameValuePairs.add(BasicNameValuePair("radius", Integer.toString(radius)))
        val post = HttpPost(INSTAGRAM_API_V1_SUBSCRIPTIONS)
        val entity: HttpEntity = UrlEncodedFormEntity(nameValuePairs)
        post.entity = entity
        return try {
            val response = commonHttpFetcher.post(post)
            log.info(response)
            val responseJSON = JSONObject(response)
            InstagramGeographySubscription(latLong, radius, responseJSON.getJSONObject("data").getLong("id"), responseJSON.getJSONObject("data").getLong("object_id"), channelId, username)
        } catch (e: HttpBadRequestException) {
            log.error("HTTP Bad request: " + e.responseBody)
            throw RuntimeException(e)
        }
    }

    @Throws(HttpFetchException::class)
    fun getSubscriptions(clientId: String, clientSecret: String): String {
        return commonHttpFetcher[INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&client_id=" + clientId]
    }

    @Throws(HttpFetchException::class)
    fun deleteAllSubscriptions(clientId: String, clientSecret: String) {
        val delete = HttpDelete(INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&object=all&client_id=" + clientId)
        log.info("Delete all response: " + commonHttpFetcher.delete(delete))
    }

    @Throws(HttpFetchException::class)
    fun deleteSubscription(id: Long, clientId: String, clientSecret: String) {
        val delete = HttpDelete(INSTAGRAM_API_V1_SUBSCRIPTIONS + "?client_secret=" + clientSecret + "&id=" + java.lang.Long.toString(id) + "&client_id=" + clientId)
        log.info("Delete subscription response; " + commonHttpFetcher.delete(delete))
    }

    @Throws(HttpFetchException::class, JSONException::class)
    fun getRecentMediaForTag(tag: String, accessToken: String): List<FeedItem> {
        val response = commonHttpFetcher["https://api.instagram.com/v1/tags/$tag/media/recent?access_token=$accessToken"]
        return parseFeedItems(response)
    }

    @Throws(HttpFetchException::class, JSONException::class)
    fun getRecentMediaForGeography(geoId: Long, clientId: String): List<FeedItem> {
        val response = commonHttpFetcher["https://api.instagram.com/v1/geographies/$geoId/media/recent?client_id=$clientId"]
        return parseFeedItems(response)
    }

    fun getAuthorizeRedirectUrl(clientId: String, redirectUrl: String): String {
        return INSTAGRAM_API_AUTHORIZE + "?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code"
    }

    @Throws(HttpFetchException::class, JSONException::class, UnsupportedEncodingException::class)
    fun getAccessToken(clientId: String?, clientSecret: String?, code: String?, redirectUrl: String?): String {
        val post = HttpPost(INSTAGRAM_API_ACCESS_TOKEN)
        val nameValuePairs: MutableList<NameValuePair> = Lists.newArrayList()
        nameValuePairs.add(BasicNameValuePair("client_id", clientId))
        nameValuePairs.add(BasicNameValuePair("client_secret", clientSecret))
        nameValuePairs.add(BasicNameValuePair("grant_type", "authorization_code"))
        nameValuePairs.add(BasicNameValuePair("redirect_uri", redirectUrl))
        nameValuePairs.add(BasicNameValuePair("code", code))
        val entity: HttpEntity = UrlEncodedFormEntity(nameValuePairs)
        post.entity = entity
        return try {
            val response = commonHttpFetcher.post(post)
            log.info("Got access token response: $response")
            val responseJson = JSONObject(response)
            responseJson.getString("access_token")
        } catch (e: HttpBadRequestException) {
            log.error(e.responseBody)
            throw RuntimeException(e)
        }
    }

    @Throws(JSONException::class)
    private fun parseFeedItems(recentMediaForTag: String): List<FeedItem> {
        val feedItems: MutableList<FeedItem> = Lists.newArrayList()
        val responseJson = JSONObject(recentMediaForTag)
        val data = responseJson.getJSONArray(DATA)
        log.info("Recent media response contains items: " + data.length())
        for (i in 0 until data.length()) {
            val imageJson = data.getJSONObject(i)
            feedItems.add(mapper.createFeedItemFrom(imageJson))
        }
        return feedItems
    }

    private fun commonSubscriptionFields(clientId: String, clientSecret: String, callbackUrl: String): MutableList<NameValuePair> {
        log.info("Callback url: $callbackUrl")
        val verifyToken = UUID.randomUUID().toString()
        val nameValuePairs: MutableList<NameValuePair> = Lists.newArrayList()
        nameValuePairs.add(BasicNameValuePair("client_id", clientId))
        nameValuePairs.add(BasicNameValuePair("client_secret", clientSecret))
        nameValuePairs.add(BasicNameValuePair("aspect", "media"))
        nameValuePairs.add(BasicNameValuePair("callback_url", callbackUrl))
        nameValuePairs.add(BasicNameValuePair("verify_token", verifyToken))
        return nameValuePairs
    }

}