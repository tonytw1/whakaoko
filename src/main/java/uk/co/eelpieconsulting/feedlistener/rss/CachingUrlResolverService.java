package uk.co.eelpieconsulting.feedlistener.rss;

import org.apache.commons.codec.digest.DigestUtils;
;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.shorturls.BitlyUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.FeedBurnerRedirectResolver;
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolverService;
import uk.co.eelpieconsulting.common.shorturls.TinyUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.TwitterShortenerUrlResolver;

@Component
public class CachingUrlResolverService {

    private static Logger log = LogManager.getLogger(CachingUrlResolverService.class);

    private static final int ONE_DAY = 3600 * 24;
    private final static String KEY_PREFIX = "resolved-urls::";

    private ShortUrlResolverService urlResolverService;
    private MemcachedCache cache;

    @Autowired
    public CachingUrlResolverService(MemcachedCache cache) {
        urlResolverService = new ShortUrlResolverService(new BitlyUrlResolver(), new FeedBurnerRedirectResolver(), new TinyUrlResolver(), new TwitterShortenerUrlResolver());
        this.cache = cache;
    }

    public String resolveUrl(String url) {
        if (url != null && !url.isEmpty()) {
            final String cachedResult = (String) cache.get(generateKey(url));
            if (cachedResult != null) {
                log.debug("Found result for url '" + url + "' in cache: " + cachedResult);
                return cachedResult;
            }

            log.debug("Delegrating to live url resolver");
            final String result = urlResolverService.resolveUrl(url);
            if (result != null) {
                putUrlIntoCache(url, result);
            }
            return result;

        } else {
            log.warn("Called with empty url");
        }
        return url;
    }

    private void putUrlIntoCache(String url, String result) {
        log.debug("Caching result for url: " + url);
        cache.put(generateKey(url), ONE_DAY, result);
    }

    private String generateKey(String id) {
        return KEY_PREFIX + DigestUtils.sha256Hex(id);
    }

}
