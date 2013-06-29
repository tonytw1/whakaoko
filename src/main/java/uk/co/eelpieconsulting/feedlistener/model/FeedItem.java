package uk.co.eelpieconsulting.feedlistener.model;

import java.io.Serializable;
import java.util.Date;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.common.views.rss.RssFeedable;

public class FeedItem implements Serializable, RssFeedable {
	
	private static final long serialVersionUID = 1L;

	private final String title;
	private final String url;
	private final String body;
	private final Date date;
	private final Place place;
	private final String imageUrl;
	
	public FeedItem(String title, String url, String body, Date date, Place place, String imageUrl) {
		this.title = title;
		this.url = url;
		this.body = body;
		this.date = date;
		this.place = place;
		this.imageUrl = imageUrl;
	}
	
	public String getId() {
		return getGUID();
	}

	private String getGUID() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}
	
	public String getBody() {
		return body;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Place getPlace() {
		return place;
	}
	
	public LatLong getLatLong() {
		return place != null ? place.getLatLong() : null;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	
	public boolean isGeoTagged() {
		return place != null;
	}

	@Override
	public String getDescription() {
		return body;
	}

	@Override
	public String getHeadline() {
		return title;
	}

	@Override
	public String getWebUrl() {
		return url;
	}

	@Override
	public String toString() {
		return "FeedItem [body=" + body + ", date=" + date + ", imageUrl="
				+ imageUrl + ", place=" + place + ", title=" + title + ", url="
				+ url + "]";
	}
	
}
