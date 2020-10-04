package uk.co.eelpieconsulting.feedlistener.rss.images;

import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.stereotype.Component;

@Component
public class HtmlImageExtractor {
	
	private static Logger log = Logger.getLogger(HtmlImageExtractor.class);
	
	public String extractImage(final String itemBody) {
		try {
			final Parser parser = new Parser();
			parser.setInputHTML(itemBody);
			NodeFilter tagNameFilter = new TagNameFilter("img");
			NodeList imageNodes = parser.extractAllNodesThatMatch(tagNameFilter);
			log.debug("Found images: " + imageNodes.size());			
			return extractFirstImage(imageNodes);
			
		} catch (ParserException e) {
			log.warn("Failed to parse item body for images", e);
		}
		return null;
	}

	private String extractFirstImage(NodeList imageNodes) {
		if (imageNodes.size() == 0) {
			return null;			
		}
		
		final Tag imageTag = (Tag) imageNodes.elementAt(0);
		final String imageSrc = imageTag.getAttribute("src");
		log.debug("Found first image: " + imageTag.toHtml() + ", " + imageSrc);
		return imageSrc;		
	}

}
