package uk.co.eelpieconsulting.feedlistener.rss;

import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndEntry;

@Component
public class RssFeedItemImageExtractor {
	
	private static Logger log = Logger.getLogger(RssFeedItemImageExtractor.class);
	
	private RssFeedItemBodyExtractor rssFeedItemBodyExtractor;
	
	@Autowired
	public RssFeedItemImageExtractor(RssFeedItemBodyExtractor rssFeedItemBodyExtractor) {
		this.rssFeedItemBodyExtractor = rssFeedItemBodyExtractor;
	}

	public String extractImageFrom(SyndEntry item) {
		final MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) item.getModule(MediaModule.URI);
		if (mediaModule != null) {
			log.debug("No media module found for item: " + item.getTitle());
		
			final MediaContent[] mediaContents = mediaModule.getMediaContents();
			if (mediaContents.length > 0) {			
				MediaContent selectedMediaContent = null;
				for (int i = 0; i < mediaContents.length; i++) {
					MediaContent mediaContent = mediaContents[i];
					final boolean isImage = isImage(mediaContent);
					if (isImage && !isBlackListed(mediaContent) && isBetterThanCurrentlySelected(mediaContent, selectedMediaContent)) {
						selectedMediaContent = mediaContent;
					}
				}
				
				if (selectedMediaContent != null) {
					log.debug("Took image reference from MediaContent: " + selectedMediaContent.getReference().toString());
					return selectedMediaContent.getReference().toString();
				}
			}
		}
		
		// Look for img srcs in html content
		final String itemBody = rssFeedItemBodyExtractor.extractBody(item);
		if (!Strings.isNullOrEmpty(itemBody)) {
			final Parser parser = new Parser();
			try {
				parser.setInputHTML(itemBody);
				NodeFilter tagNameFilter = new TagNameFilter("img");
				NodeList imageNodes = parser.extractAllNodesThatMatch(tagNameFilter);
				log.debug("Found images: " + imageNodes.size());
				for (int i = 0; i < imageNodes.size(); i++) {
					final Tag imageTag = (Tag) imageNodes.elementAt(0);
					final String imageSrc = imageTag.getAttribute("src");
					if (!isBlackListedUrl(imageSrc)) {
						log.info("Found first image: " + imageTag.toHtml() + ", " + imageSrc);
						return imageSrc;
					}
				}
								
			} catch (ParserException e) {
				log.warn("Failed to parse item body for images", e);
			}		
		}
		
		log.debug("No suitable media element image seen");
		return null;
	}
	
	private boolean isBlackListed(MediaContent mediaContent) {
		if (mediaContent.getReference() != null) {
			return isBlackListedUrl(mediaContent.getReference().toString());
		}
		return false;
	}

	private boolean isBlackListedUrl(String url) {
		return url.startsWith("http://stats.wordpress.com");
	}
	
	private boolean isImage(MediaContent mediaContent) {
		final boolean hasTypeJpegAttribute = mediaContent.getType() != null && mediaContent.getType().equals("image/jpeg");
		final boolean isJpegUrl = mediaContent.getReference() != null && mediaContent.getReference().toString().endsWith("jpg");	 // TODO test cover and .
		return mediaContent.getReference() != null && (hasTypeJpegAttribute || isJpegUrl);
	}
	
	private boolean isBetterThanCurrentlySelected(MediaContent mediaContent, MediaContent selectedMediaContent) {
		if (selectedMediaContent == null) {
			return true;
		}		
		return mediaContent.getWidth() != null && mediaContent.getWidth() > selectedMediaContent.getWidth();		
	}
	
}
