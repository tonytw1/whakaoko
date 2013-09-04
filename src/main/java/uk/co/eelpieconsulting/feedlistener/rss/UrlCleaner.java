package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class UrlCleaner {
	
    private static final String HTTP_PREFIX = "http://";
	private static final String PHP_SESSION_REGEX = "[&\\?]PHPSESSID=[0-9|a-f]{32}";

	private static final Pattern phpSessionPattern = Pattern.compile(PHP_SESSION_REGEX);
    
	public String cleanSubmittedItemUrl(String url) {
		String cleanedUrl = new String(url);
		cleanedUrl = trimWhiteSpace(cleanedUrl);
		cleanedUrl = addHttpPrefixIfMissing(cleanedUrl);
		cleanedUrl = stripFeedburnerParams(cleanedUrl);
		cleanedUrl = stripPhpSession(cleanedUrl);
		return cleanedUrl;
	}
	
    private String trimWhiteSpace(String title) {
        return title.trim();
    }
    
    private String addHttpPrefixIfMissing(String url) {
        if (!Strings.isNullOrEmpty(url) && !hasHttpPrefix(url)) {
            url = addHttpPrefix(url);
        }
        return url;
    }
    
    private static boolean hasHttpPrefix(String url) {  
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    private static String addHttpPrefix(String url) {
        url = HTTP_PREFIX + url;
        return url;
    }
    
	private static String stripFeedburnerParams(String url) {
		Pattern p = Pattern.compile("[&|?]utm_.*(.*)$");
        return p.matcher(url).replaceAll("");
	}
	
	private String stripPhpSession(String url) {
		return phpSessionPattern.matcher(url).replaceAll("");
	}
    
}
