package uk.co.eelpieconsulting.feedlistener.rss;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.GeoRSSUtils;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

@Component
public class FeedFetcher {
	
	private static Logger log = Logger.getLogger(FeedFetcher.class);
	
	@SuppressWarnings("unchecked")
	public FetchedFeed fetchFeed(String url) {
		final List<FeedItem> feedItems = new ArrayList<FeedItem>();
    	final SyndFeed syndfeed = loadSyndFeedWithFeedFetcher(url);
    	if (syndfeed == null) {
    		log.warn("Could not load syndfeed from url: " + url + ". Returning empty list of items");
    		return null;
    	}
    	
        Iterator<SyndEntry> feedItemsIterator = syndfeed.getEntries().iterator();
        while (feedItemsIterator.hasNext()) {        	
        	final SyndEntry syndEntry = (SyndEntry) feedItemsIterator.next();
        	final LatLong latLong = extractLocationFrom(syndEntry);        	
			final String imageUrl = extractImageFrom(syndEntry);			
        	final String body = syndEntry.getDescription() != null ? syndEntry.getDescription().getValue() : null;
			feedItems.add(new FeedItem(syndEntry.getTitle(), syndEntry.getUri(), body, syndEntry.getLink(), syndEntry.getPublishedDate(), latLong, imageUrl));
        }
        
        return new FetchedFeed(syndfeed.getTitle(), feedItems);
	}
	
	private SyndFeed loadSyndFeedWithFeedFetcher(String feedUrl) {
		log.info("Loading SyndFeed from url: " + feedUrl);
		try {
			HttpURLFeedFetcher fetcher = new HttpURLFeedFetcher();	// TODO thread safe for reuse?
			SyndFeed feed = fetcher.retrieveFeed(new URL(feedUrl));
			return feed;			
		} catch (Exception e) {
			log.warn("Error while fetching feed: " + e.getMessage());
		}
		return null;
	}
	
	private LatLong extractLocationFrom(SyndEntry syndEntry) {
		final GeoRSSModule geoModule = (GeoRSSModule) GeoRSSUtils.getGeoRSS(syndEntry);
		if (geoModule != null && geoModule.getPosition() != null) {
			final LatLong latLong = new LatLong(geoModule.getPosition().getLatitude(), geoModule.getPosition().getLongitude());
			log.debug("Rss item '" + syndEntry.getTitle() + "' has position information: " + latLong);
			return latLong;
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
		return mediaContent.getWidth() > selectedMediaContent.getWidth();		
	}
	
}
