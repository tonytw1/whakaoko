package uk.co.eelpieconsulting.feedlistener.http

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpHead
import com.github.kittinunf.result.Result
import org.apache.log4j.Logger

class HttpFetcher(val userAgent: String, val timeout: Int) {

    private val log = Logger.getLogger(HttpFetcher::class.java)

    fun head(url: String): Result<Headers, Exception>  { //  TODO status code
        val (request, response, result) = url.httpHead().
        timeout(timeout).header("User-Agent", userAgent).
        response()

        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                log.warn("Failed to head url: " + url, ex)
                return Result.error(ex)
            }
            is Result.Success -> {
                return Result.success(response.headers)
            }
        }
    }

    fun getBytes(url: String): Result<Pair<ByteArray, Headers>, Exception> {
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
                return Result.success(Pair(result.get(), response.headers))
            }
        }
    }

}
