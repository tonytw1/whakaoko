package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.common.base.Strings;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.GeoRSSUtils;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;

import uk.co.eelpieconsulting.common.html.*;

@Component
public class RssFeedItemMapper {
	
	private static Logger log = Logger.getLogger(RssFeedItemMapper.class);
	
	public FeedItem createFeedItemFrom(final SyndEntry syndEntry) {
		final Place place = extractLocationFrom(syndEntry);        	
		final String imageUrl = extractImageFrom(syndEntry);		
		final String body = new HtmlCleaner().stripHtml(StringEscapeUtils.unescapeHtml(extractBody(syndEntry)));		
		final Date date = syndEntry.getPublishedDate() != null ? syndEntry.getPublishedDate() : syndEntry.getUpdatedDate();						
		final FeedItem feedItem = new FeedItem(syndEntry.getTitle(), syndEntry.getLink().trim(), body, date, place, imageUrl);
		return feedItem;
	}

	@SuppressWarnings("unchecked")
	private String extractBody(final SyndEntry syndEntry) {
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
	
	private Place extractLocationFrom(SyndEntry syndEntry) {
		final GeoRSSModule geoModule = (GeoRSSModule) GeoRSSUtils.getGeoRSS(syndEntry);
		if (geoModule != null && geoModule.getPosition() != null) {
			final LatLong latLong = new LatLong(geoModule.getPosition().getLatitude(), geoModule.getPosition().getLongitude());
			log.debug("Rss item '" + syndEntry.getTitle() + "' has position information: " + latLong);
			return new Place(null, latLong, null);
		}
		return null;
	}
	
	private String extractImageFrom(SyndEntry item) {
		final MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) item.getModule(MediaModule.URI);
		if (mediaModule != null) {
			log.debug("No media module found for item: " + item.getTitle());
		
			final MediaContent[] mediaContents = mediaModule.getMediaContents();
			if (mediaContents.length > 0) {			
				MediaContent selectedMediaContent = null;
				for (int i = 0; i < mediaContents.length; i++) {
					MediaContent mediaContent = mediaContents[i];
					final boolean isImage = isImage(mediaContent);
					if (isImage && isBetterThanCurrentlySelected(mediaContent, selectedMediaContent)) {
						selectedMediaContent = mediaContent;
					}
				}
				
				if (selectedMediaContent != null) {
					log.debug("Took image reference from MediaContent: " + selectedMediaContent.getReference().toString());
					return selectedMediaContent.getReference().toString();
				}
			}
		}
		
		// Look got img srcs in html content
		final String itemBody = extractBody(item);
		if (!Strings.isNullOrEmpty(itemBody)) {
			Parser parser = new Parser();
			try {
				parser.setInputHTML(itemBody);
				NodeFilter tagNameFilter = new TagNameFilter("img");
				NodeList imageNodes = parser.extractAllNodesThatMatch(tagNameFilter);
				log.info("Found images: " + imageNodes.size());
				if (imageNodes.size() > 0) {
					final Tag imageTag = (Tag) imageNodes.elementAt(0);
					final String imageSrc = imageTag.getAttribute("src");
					log.info("Found first image: " + imageTag.toHtml() + ", " + imageSrc);
					return imageSrc;
				}
								
			} catch (ParserException e) {
				log.warn("Failed to parse item body for images", e);
			}			
		}
		
		log.debug("No suitable media element image seen");
		return null;
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
	
	private String getItemDescription(final SyndEntry syndEntry) {
		return syndEntry.getDescription() != null ? syndEntry.getDescription().getValue() : null;
	}

}
