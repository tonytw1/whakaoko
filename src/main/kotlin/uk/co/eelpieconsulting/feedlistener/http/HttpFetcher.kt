package uk.co.eelpieconsulting.feedlistener.http

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpHead
import com.github.kittinunf.result.Result
import org.apache.logging.log4j.LogManager
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HttpFetcher(val userAgent: String, val timeout: Int) {

    private val log = LogManager.getLogger(HttpFetcher::class.java)

    fun head(url: String): Result<Pair<Headers, Int>, FuelError>  {
        val (_, response, result) = url.httpHead().
        timeout(timeout).header("User-Agent", userAgent).
        response()

        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                log.warn("Failed to head url '" + url + "'. Status code was " + ex.response.statusCode + " and exception was " + ex.message)
                return Result.error(ex)
            }
            is Result.Success -> {
                log.info("Head response code is: " + response.statusCode)
                return Result.success(Pair(response.headers, response.statusCode))
            }
        }
    }

    fun get(url: String, etag: String?, lastModified: Date?): Result<HttpResult, FuelError> {
        val request = url.httpGet().timeout(timeout).header("User-Agent", userAgent)
        etag?.let {
            request.header("If-None-Match", etag)
        }

        lastModified?.let {
            val dateTime = ZonedDateTime.ofInstant(lastModified.toInstant(), ZoneOffset.UTC)
            val ifModifiedSince = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
            log.info("Appending If-Modified-Since header to request: $ifModifiedSince")
            request.header("If-Modified-Since", ifModifiedSince)
        }

        val (_, response, result) = request.response()
        result.fold({ bytes ->
            val httpResult = HttpResult(bytes = bytes, status = response.statusCode, headers = response.headers)
            return Result.success(httpResult)

        }, { fuelError ->
            log.warn("Failed to fetch from url: " + url + "; status code was: " + fuelError.response.statusCode, fuelError.message)
            return Result.error(fuelError)
        })
    }

}

class HttpResult(val bytes: ByteArray, val status: Int, val headers: Headers)