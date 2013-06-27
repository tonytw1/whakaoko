package uk.co.eelpieconsulting.feedlistener.model;

public class RssSubscription extends Subscription {
	
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

	@Override
	public String getId() {
		return "feeds/" + url;
	}
	
}
