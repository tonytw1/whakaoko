package uk.co.eelpieconsulting.feedlistener.rss

import net.spy.memcached.MemcachedClient
import org.apache.commons.codec.digest.DigestUtils
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolverService

@Component
class UrlResolverService @Autowired constructor(private val shortUrlResolverService: ShortUrlResolverService, private val memcachedClient: MemcachedClient) {

    private val log = LogManager.getLogger(UrlResolverService::class.java)
    private val ONE_DAY = 3600 * 24
    private val KEY_PREFIX = "resolved-urls::"

    fun resolveUrl(url: String?): String? {
        return if (url != null && !url.isEmpty()) {
            val key = cacheKeyFor(url)
            val cachedResult = memcachedClient[key] as String
            if (cachedResult != null) {
                log.debug("Found result for url '$url' in cache: $cachedResult")
                return cachedResult
            }
            log.debug("Delegating to live url resolver")
            val result = shortUrlResolverService.resolveUrl(url)
            if (result != null) {
                log.debug("Caching result for url: $url")
                memcachedClient.add(key, ONE_DAY, result)
            }
            result
        } else {
            log.warn("Called with empty url; not attempting to resolve")
            url
        }
    }

    private fun cacheKeyFor(id: String): String {
        return KEY_PREFIX + DigestUtils.sha256Hex(id)
    }

}