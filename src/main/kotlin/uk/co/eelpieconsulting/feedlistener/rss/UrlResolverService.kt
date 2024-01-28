package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import net.spy.memcached.MemcachedClient
import org.apache.commons.codec.digest.DigestUtils
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolver
import java.net.URL

@Component
class UrlResolverService @Autowired constructor(private val shortUrlResolver: ShortUrlResolver, private val memcachedClient: MemcachedClient) {

    private val log = LogManager.getLogger(UrlResolverService::class.java)
    private val oneWeek = 3600 * 24 * 7
    private val keyPrefix = "resolved-urls::"

    fun resolveUrl(url: String?): String? {
        if (!Strings.isNullOrEmpty(url)) {
            val parsedUrl = URL(url)    // TODO exceptions

            if (!shortUrlResolver.isValid(parsedUrl)) {
                // If the resolver does not support this url we can return early.
                // No need to fill the cache with uninteresting resolutions.
                return url
            }

            // Resolve and cache urls which the resolver has expressed an interest in
            val key = cacheKeyFor(parsedUrl.toExternalForm())
            val cachedResult = memcachedClient[key] as String?
            cachedResult?.let {
                log.info("Found result for url '$url' in cache: $cachedResult")
                return it

            } ?: run {
                log.debug("Delegating to live url resolver")
                val result = shortUrlResolver.resolveUrl(parsedUrl)
                log.info("Caching result for url: $url")
                memcachedClient.add(key, oneWeek, result.toExternalForm())
                return result.toExternalForm()
            }

        } else {
            log.warn("Called with empty url; not attempting to resolve")
            return url
        }
    }

    private fun cacheKeyFor(id: String): String {
        return keyPrefix + DigestUtils.sha256Hex(id)
    }

}