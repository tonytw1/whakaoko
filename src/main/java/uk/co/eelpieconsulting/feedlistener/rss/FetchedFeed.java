package uk.co.eelpieconsulting.feedlistener.rss;

import java.io.Serializable;
import java.util.List;

import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

public class FetchedFeed implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String feedName, etag;
	private final List<FeedItem> feedItems;
	
	public FetchedFeed(String feedName, List<FeedItem> feedItems, String etag) {
		this.feedName = feedName;
		this.feedItems = feedItems;
		this.etag = etag;
	}
	
	public String getFeedName() {
		return feedName;
	}

	public List<FeedItem> getFeedItems() {
		return feedItems;
	}

	public String getEtag() {
		return etag;
	}

	@Override
	public String toString() {
		return "FetchedFeed{" +
				"feedName='" + feedName + '\'' +
				", etag='" + etag + '\'' +
				", feedItems=" + feedItems +
				'}';
	}
}
