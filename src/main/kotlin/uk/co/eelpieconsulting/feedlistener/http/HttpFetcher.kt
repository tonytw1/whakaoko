package uk.co.eelpieconsulting.feedlistener.http

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.apache.logging.log4j.LogManager
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HttpFetcher(private val userAgent: String, private val timeout: Int) {

    private val log = LogManager.getLogger(HttpFetcher::class.java)

    suspend fun get(url: String, etag: String?, lastModified: Date?): Result<HttpResult, FuelError> {
        val request = withCommonRequestProperties(url.httpGet())
        etag?.let {
            request.header("If-None-Match", etag)
        }

        lastModified?.let {
            val dateTime = ZonedDateTime.ofInstant(lastModified.toInstant(), ZoneOffset.UTC)
            val ifModifiedSince = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
            log.info("Appending If-Modified-Since header to request: $ifModifiedSince")
            request.header("If-Modified-Since", ifModifiedSince)
        }

        val (_, response, result) = request.awaitByteArrayResponseResult()
        return result.fold({ bytes ->
            val httpResult = HttpResult(bytes = bytes, status = response.statusCode, headers = response.headers)
            Result.success(httpResult)

        }, { fuelError ->
            log.warn(
                "Failed to fetch from url: " + url + "; status code was: " + fuelError.response.statusCode,
                fuelError.message
            )
            Result.error(fuelError)
        })
    }

    private fun withCommonRequestProperties(request: Request): Request {
        return request.timeout(timeout).timeoutRead(timeout).header("user-agent", userAgent).header("accept", "*/*")
    }

}

class HttpResult(val bytes: ByteArray, val status: Int, val headers: Headers)