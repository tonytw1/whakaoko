package uk.co.eelpieconsulting.feedlistener.rss

import com.google.common.base.Strings
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class UrlCleaner {
    fun cleanSubmittedItemUrl(url: String): String {
        var cleanedUrl = java.lang.String(url) as String
        cleanedUrl = trimWhiteSpace(cleanedUrl)
        cleanedUrl = addHttpPrefixIfMissing(cleanedUrl)
        cleanedUrl = stripFeedburnerParams(cleanedUrl)
        cleanedUrl = stripPhpSession(cleanedUrl)
        return cleanedUrl
    }

    private fun trimWhiteSpace(title: String): String {
        return title.trim { it <= ' ' }
    }

    private fun addHttpPrefixIfMissing(url: String): String {
        return if (!Strings.isNullOrEmpty(url) && !hasHttpPrefix(url)) {
            addHttpPrefix(url)
        } else {
            url
        }
    }

    private fun stripPhpSession(url: String): String {
        return phpSessionPattern.matcher(url).replaceAll("")
    }

    companion object {
        private const val HTTP_PREFIX = "http://"
        private const val PHP_SESSION_REGEX = "[&\\?]PHPSESSID=[0-9|a-f]{32}"
        private val phpSessionPattern = Pattern.compile(PHP_SESSION_REGEX)
        private fun hasHttpPrefix(url: String): Boolean {
            return url.startsWith("http://") || url.startsWith("https://")
        }

        private fun addHttpPrefix(url: String): String {
            return HTTP_PREFIX + url
        }

        private fun stripFeedburnerParams(url: String): String {
            val p = Pattern.compile("[&|?]utm_.*(.*)$")
            return p.matcher(url).replaceAll("")
        }
    }
}
