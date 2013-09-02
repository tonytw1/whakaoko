package uk.co.eelpieconsulting.feedlistener.model;

import org.apache.commons.codec.digest.DigestUtils;

public class RssSubscription extends Subscription {
	
	private String url;
	
	public RssSubscription() {
	}

	public RssSubscription(String url, String channelId) {
		this.setId("feed-" + DigestUtils.md5Hex(url));
		this.setChannelId(channelId);
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
		return "RssSubscription [url=" + url + ", getChannelId()="
				+ getChannelId() + ", getLastRead()=" + getLastRead()
				+ ", getLatestItemDate()=" + getLatestItemDate()
				+ ", getName()=" + getName() + "]";
	}
	
}
