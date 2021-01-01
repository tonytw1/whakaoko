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
    private val ONE_DAY = 3600 * 24
    private val KEY_PREFIX = "resolved-urls::"

    fun resolveUrl(url: String?): String? {
        if (!Strings.isNullOrEmpty(url)) {
            val parsedUrl = URL(url)    // TODO exceptions

            if (!shortUrlResolver.isValid(parsedUrl)) {
                log.info("Skipping resolve short url which is not supported by resolver: " + parsedUrl.toExternalForm())
                return url
            }

            val key = cacheKeyFor(parsedUrl.toExternalForm())

            val cachedResult = memcachedClient[key] as String?

            cachedResult?.let {
                log.info("Found result for url '$url' in cache: $cachedResult")
                return it

            } ?: run {
                log.debug("Delegating to live url resolver")
                val result = shortUrlResolver.resolveUrl(parsedUrl)
                log.info("Caching result for url: $url")
                memcachedClient.add(key, ONE_DAY, result.toExternalForm())
                return result.toExternalForm()
            }

        } else {
            log.warn("Called with empty url; not attempting to resolve")
            return url
        }
    }

    private fun cacheKeyFor(id: String): String {
        return KEY_PREFIX + DigestUtils.sha256Hex(id)
    }

}