package uk.co.eelpieconsulting.feedlistener.http

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.apache.log4j.Logger

class HttpFetcher(val userAgent: String, val timeout: Int) {

    private val log = Logger.getLogger(HttpFetcher::class.java)

    fun getBytes(url: String): Result<Pair<ByteArray, String?>, Exception> {
        val (request, response, result) = url.httpGet().
        timeout(timeout).header("User-Agent", userAgent).
        response()

        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                log.warn("Failed to fetch from url: " + url, ex)
                return Result.error(ex)
            }
            is Result.Success -> {
                val etag = response.header("ETag").firstOrNull()
                val value: Pair<ByteArray, String?> = Pair(result.get(), etag)
                return Result.success(value)
            }
        }
    }

}
