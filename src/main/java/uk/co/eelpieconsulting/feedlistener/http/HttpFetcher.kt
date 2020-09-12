package uk.co.eelpieconsulting.feedlistener.http

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.apache.log4j.Logger

class HttpFetcher(val userAgent: String, val timeout: Int) {

    private val log = Logger.getLogger(HttpFetcher::class.java)

    fun getBytes(url: String): Pair<ByteArray, String?>? {
        val (request, response, result) = url.httpGet().
        timeout(timeout).header("User-Agent", userAgent).
        response()

        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                log.warn("Failed to fetch from url: " + url, ex)
                return null
            }
            is Result.Success -> {
                val etag = response.header("ETag").firstOrNull()
                return Pair(result.get(), etag)
            }
        }
    }

}
