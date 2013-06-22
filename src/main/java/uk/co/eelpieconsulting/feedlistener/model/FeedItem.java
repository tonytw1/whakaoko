package uk.co.eelpieconsulting.feedlistener.model;

import java.io.Serializable;
import java.util.Date;

import uk.co.eelpieconsulting.common.geo.model.LatLong;

public class FeedItem implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String title;
	private final String url;
	private final String body;
	private final String link;
	private final Date date;
	private final LatLong latLong;
	private final String imageUrl;

	public FeedItem(String title, String url, String body, String link, Date date, LatLong latLong, String imageUrl) {
		this.title = title;
		this.url = url;
		this.body = body;
		this.link = link;
		this.date = date;
		this.latLong = latLong;
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

	public String getLink() {
		return link;
	}

	public Date getDate() {
		return date;
	}
	
	public LatLong getLatLong() {
		return latLong;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	
	public boolean isGeoTagged() {
		return latLong != null;
	}

	@Override
	public String toString() {
		return "FeedItem [body=" + body + ", date=" + date + ", imageUrl="
				+ imageUrl + ", latLong=" + latLong + ", link=" + link
				+ ", title=" + title + ", uri=" + url + "]";
	}
	
}
