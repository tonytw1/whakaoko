package uk.co.eelpieconsulting.feedlistener.model;

public class RssSubscription extends Subscription {
	
	private String url;
	
	public RssSubscription() {
	}

	public RssSubscription(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
