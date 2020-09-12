package uk.co.eelpieconsulting.feedlistener.http

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.apache.log4j.Logger
import uk.co.eelpieconsulting.feedlistener.rss.FeedFetcher

class HttpFetcher(responseCharacterEncoding: String, userAgent: String, val timeout: Int) {

    private val log = Logger.getLogger(FeedFetcher::class.java)

    private val httpFetcher: uk.co.eelpieconsulting.common.http.HttpFetcher =
            uk.co.eelpieconsulting.common.http.HttpFetcher(responseCharacterEncoding, userAgent, timeout)

    fun getBytes(url: String): ByteArray? {
        val (request, response, result) = url
                .httpGet()
                .response()

        when(result) {
            is Result.Failure -> {
                val ex = result.getException()
                log.warn("Failed to fetch from url: " + url, ex)
                return null
            }
            is Result.Success -> {
                return result.get()
            }
        }
    }

}
