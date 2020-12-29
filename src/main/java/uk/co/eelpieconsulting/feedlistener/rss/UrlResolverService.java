package uk.co.eelpieconsulting.feedlistener.rss;

import net.spy.memcached.MemcachedClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolverService;

@Component
public class UrlResolverService {

    private static Logger log = LogManager.getLogger(UrlResolverService.class);

    private static final int ONE_DAY = 3600 * 24;
    private final static String KEY_PREFIX = "resolved-urls::";

    private ShortUrlResolverService shortUrlResolverService;
    private MemcachedClient memcachedClient;

    @Autowired
    public UrlResolverService(ShortUrlResolverService shortUrlResolverService, MemcachedClient memcachedClient) {
        this.shortUrlResolverService = shortUrlResolverService;
        this.memcachedClient = memcachedClient;
    }

    public String resolveUrl(String url) {
        if (url != null && !url.isEmpty()) {
            final String key = cacheKeyFor(url);
            final String cachedResult = (String) memcachedClient.get(key);
            if (cachedResult != null) {
                log.debug("Found result for url '" + url + "' in cache: " + cachedResult);
                return cachedResult;
            }

            log.debug("Delegating to live url resolver");
            final String result = shortUrlResolverService.resolveUrl(url);
            if (result != null) {
                log.debug("Caching result for url: " + url);
                memcachedClient.add(key, ONE_DAY, result);
            }
            return result;

        } else {
            log.warn("Called with empty url; not attempting to resolve");
            return url;
        }
    }

    private String cacheKeyFor(String id) {
        return KEY_PREFIX + DigestUtils.sha256Hex(id);
    }

}
