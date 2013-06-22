package uk.co.eelpieconsulting.feedlistener.model;

public class RssSubscription {
	
	private final String url;

	public RssSubscription(String url) {
		super();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return "RssSubscription [url=" + url + "]";
	}
	
}
