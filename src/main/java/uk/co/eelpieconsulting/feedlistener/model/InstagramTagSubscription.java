package uk.co.eelpieconsulting.feedlistener.model;

import org.apache.commons.codec.digest.DigestUtils;

public class InstagramTagSubscription extends InstagramSubscription {

	private String tag;
	
	public InstagramTagSubscription() {
	}

	public InstagramTagSubscription(String tag, long subscriptionId, String channelId, String username) {
		this.setId("instagram-" + DigestUtils.md5Hex("tag" + tag));
		this.tag = tag;
		this.subscriptionId = subscriptionId;
		this.setName("Instagram - " + tag);
		this.setChannelId(channelId);
		this.setUsername(username);
	}
	
	public String getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return "InstagramTagSubscription [subscriptionId=" + subscriptionId + ", tag=" + tag + "]";
	}
	
}
