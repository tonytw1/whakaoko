package uk.co.eelpieconsulting.feedlistener.rss;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.common.html.HtmlCleaner;
import uk.co.eelpieconsulting.common.shorturls.BitlyUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.FeedBurnerRedirectResolver;
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolverService;
import uk.co.eelpieconsulting.common.shorturls.TinyUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.TwitterShortenerUrlResolver;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.GeoRSSUtils;
import com.sun.syndication.feed.synd.SyndEntry;

@Component
public class RssFeedItemMapper {
	
	private static Logger log = Logger.getLogger(RssFeedItemMapper.class);
	
	private RssFeedItemImageExtractor rssFeedItemImageExtractor;
	private RssFeedItemBodyExtractor rssFeedItemBodyExtractor;
	private ShortUrlResolverService shortUrlResolverService;
		
	@Autowired
	public RssFeedItemMapper(RssFeedItemImageExtractor rssFeedItemImageExtractor, RssFeedItemBodyExtractor rssFeedItemBodyExtractor) {
		this.rssFeedItemImageExtractor = rssFeedItemImageExtractor;
		this.rssFeedItemBodyExtractor = rssFeedItemBodyExtractor;
		this.shortUrlResolverService = new ShortUrlResolverService(new BitlyUrlResolver(), new FeedBurnerRedirectResolver(), new TinyUrlResolver(), new TwitterShortenerUrlResolver());	// TODO factory method from library required
	}
	
	public FeedItem createFeedItemFrom(final SyndEntry syndEntry) {
		final Place place = extractLocationFrom(syndEntry);        	
		final String imageUrl = rssFeedItemImageExtractor.extractImageFrom(syndEntry);		
		final String body = new HtmlCleaner().stripHtml(StringEscapeUtils.unescapeHtml(rssFeedItemBodyExtractor.extractBody(syndEntry)));		
		final Date date = syndEntry.getPublishedDate() != null ? syndEntry.getPublishedDate() : syndEntry.getUpdatedDate();						
		final FeedItem feedItem = new FeedItem(syndEntry.getTitle(), extractUrl(syndEntry), body, date, place, imageUrl);
		return feedItem;
	}

	private String extractUrl(final SyndEntry syndEntry) {
		final String url = syndEntry.getLink().trim();
		return shortUrlResolverService.resolveUrl(url);
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

}
