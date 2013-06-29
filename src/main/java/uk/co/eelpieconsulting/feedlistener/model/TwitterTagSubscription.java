package uk.co.eelpieconsulting.feedlistener.model;

import org.apache.commons.codec.digest.DigestUtils;

public class TwitterTagSubscription extends Subscription {

	private String tag;
	
	public TwitterTagSubscription() {
	}

	public TwitterTagSubscription(String tag) {
		this.setTag(tag);
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
		this.setName(generateName(tag));
	}
	
	@Override
	public String getId() {
		return "twitter-" + DigestUtils.md5Hex("tag" + tag);
	}

	@Override
	public String toString() {
		return "TwitterTagSubscription [tag=" + tag + "]";
	}


	private String generateName(String tag) {
		return "Twitter - " + tag;
	}
	
}
