package uk.co.eelpieconsulting.feedlistener.model;

import dev.morphia.annotations.Entity;
import org.apache.commons.codec.digest.DigestUtils;

@Entity("subscriptions")
public class TwitterTagSubscription extends Subscription {

	private String tag;
	
	public TwitterTagSubscription() {
	}

	public TwitterTagSubscription(String tag, String channel, String username) {
		this.setId("twitter-" + DigestUtils.md5Hex("tag" + tag));
		this.setTag(tag);
		this.setChannelId(channel);
		this.setUsername(username);
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
		this.setName(generateName(tag));
	}

	@Override
	public String toString() {
		return "TwitterTagSubscription [tag=" + tag + "]";
	}
	
	private String generateName(String tag) {
		return "Twitter - " + tag;
	}
	
}
