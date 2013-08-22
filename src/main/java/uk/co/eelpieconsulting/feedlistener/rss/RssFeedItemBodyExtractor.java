package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;

@Component
public class RssFeedItemBodyExtractor {

	private static Logger log = Logger.getLogger(RssFeedItemBodyExtractor.class);
	
	@SuppressWarnings("unchecked")
	public String extractBody(final SyndEntry syndEntry) {
		String body = getItemDescription(syndEntry);
		if (!Strings.isNullOrEmpty(body)) {
			return body;
		}
		
		log.info("No description tag found; looking for contents");
		final List<SyndContentImpl> contents = syndEntry.getContents();
		if (!contents.isEmpty()) {
			final String contentBody = contents.get(0).getValue();
			return contentBody;
		}
		
		return null;		
	}
	
	private String getItemDescription(final SyndEntry syndEntry) {
		return syndEntry.getDescription() != null ? syndEntry.getDescription().getValue() : null;
	}
	
}
