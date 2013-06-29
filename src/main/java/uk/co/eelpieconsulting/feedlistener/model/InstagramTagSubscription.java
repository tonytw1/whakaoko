package uk.co.eelpieconsulting.feedlistener.model;

import org.apache.commons.codec.digest.DigestUtils;

public class InstagramTagSubscription extends Subscription {

	private String tag;
	private long subscriptionId;
	
	public InstagramTagSubscription() {
	}

	public InstagramTagSubscription(String tag, long subscriptionId) {
		this.setId("instagram-" + DigestUtils.md5Hex("tag" + tag));
		this.setTag(tag);
		this.subscriptionId = subscriptionId;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
		this.setName("Instagram - " + tag);
	}
	
	public long getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	@Override
	public String toString() {
		return "InstagramTagSubscription [subscriptionId=" + subscriptionId + ", tag=" + tag + "]";
	}
	
}
