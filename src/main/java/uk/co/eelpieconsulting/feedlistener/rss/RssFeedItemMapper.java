package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
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
import com.sun.syndication.feed.synd.SyndEntry;

@Component
public class RssFeedItemMapper {
	
	private static Logger log = Logger.getLogger(RssFeedItemMapper.class);
	
	public FeedItem createFeedItemFrom(final SyndEntry syndEntry) {
		final Place place = extractLocationFrom(syndEntry);        	
		final String imageUrl = extractImageFrom(syndEntry);
		
		String body = syndEntry.getDescription() != null ? syndEntry.getDescription().getValue() : null;
		if (!Strings.isNullOrEmpty(body)) {
			body = StringEscapeUtils.unescapeHtml(body);
		}
		
		final Date date = syndEntry.getPublishedDate() != null ? syndEntry.getPublishedDate() : syndEntry.getUpdatedDate();						
		final FeedItem feedItem = new FeedItem(syndEntry.getTitle(), syndEntry.getLink().trim(), body, date, place, imageUrl);
		return feedItem;
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
		if (mediaModule == null) {
			log.debug("No media module found for item: " + item.getTitle());
			return null;
		}
		
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

}
