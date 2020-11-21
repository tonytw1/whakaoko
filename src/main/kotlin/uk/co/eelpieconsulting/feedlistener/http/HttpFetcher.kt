package uk.co.eelpieconsulting.feedlistener.http

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpHead
import com.github.kittinunf.result.Result
import org.apache.logging.log4j.LogManager

class HttpFetcher(val userAgent: String, val timeout: Int) {

    private val log = LogManager.getLogger(HttpFetcher::class.java)

    fun head(url: String): Result<Pair<Headers, Int>, FuelError>  {
        val (_, response, result) = url.httpHead().
        timeout(timeout).header("User-Agent", userAgent).
        response()

        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                log.warn("Failed to head url: " + url, ex)
                return Result.error(ex)
            }
            is Result.Success -> {
                log.info("Head response code is: " + response.statusCode)
                return Result.success(Pair(response.headers, response.statusCode))
            }
        }
    }

    fun getBytes(url: String): Result<HttpResult, FuelError> {
        val (_, response, result) = url.httpGet().
        timeout(timeout).header("User-Agent", userAgent).response()

        result.fold({ bytes ->
            val httpResult = HttpResult(bytes = bytes, status = response.statusCode, headers = response.headers)
            return Result.success(httpResult)

        }, { fuelError ->
            log.warn("Failed to fetch from url: " + url + "; status code was: " + fuelError.response.statusCode, fuelError)
            return Result.error(fuelError)
        })
    }

}

class HttpResult(val bytes: ByteArray, val status: Int, val headers: Headers)