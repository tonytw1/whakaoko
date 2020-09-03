package uk.co.eelpieconsulting.feedlistener.model;

import dev.morphia.annotations.Entity;
import org.apache.commons.codec.digest.DigestUtils;

@Entity("subscriptions")
public class RssSubscription extends Subscription {
	
	private String url;
	
	public RssSubscription() {
	}

	public RssSubscription(String url, String channelId, String username) {
		this.setId(channelId + "-" + "feed-" + DigestUtils.md5Hex(url));
		this.setChannelId(channelId);
		this.setUsername(username);
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
