package uk.co.eelpieconsulting.feedlistener.rss;

import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class HtmlImageExtractor {
	
	private static Logger log = Logger.getLogger(HtmlImageExtractor.class);
	
	public String extractImage(final String itemBody) {
		try {
			final Parser parser = new Parser();
			parser.setInputHTML(itemBody);
			NodeFilter tagNameFilter = new TagNameFilter("img");
			NodeList imageNodes = parser.extractAllNodesThatMatch(tagNameFilter);
			log.debug("Found images: " + imageNodes.size());
			for (int i = 0; i < imageNodes.size(); i++) {
				final Tag imageTag = (Tag) imageNodes.elementAt(0);
				final String imageSrc = imageTag.getAttribute("src");
				log.debug("Found first image: " + imageTag.toHtml() + ", " + imageSrc);
				return imageSrc;			
			}
			
		} catch (ParserException e) {
			log.warn("Failed to parse item body for images", e);
		}
		return null;
	}

}
