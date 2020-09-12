package uk.co.eelpieconsulting.feedlistener.http

import uk.co.eelpieconsulting.common.http.HttpFetchException

class HttpFetcher(responseCharacterEncoding: String, userAgent: String, timeout: Int) {

    private val httpFetcher: uk.co.eelpieconsulting.common.http.HttpFetcher =
            uk.co.eelpieconsulting.common.http.HttpFetcher(responseCharacterEncoding, userAgent, timeout)

    @Throws(HttpFetchException::class)
    fun getBytes(url: String): ByteArray {
        return httpFetcher.getBytes(url)
    }

}
