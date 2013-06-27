package uk.co.eelpieconsulting.feedlistener.model;

public class TwitterTagSubscription extends Subscription {

	private final String tag;

	public TwitterTagSubscription(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}
	
	@Override
	public String getId() {
		return "twitter/tags/" + tag;
	}

	@Override
	public String toString() {
		return "TwitterTagSubscription [tag=" + tag + "]";
	}

	
}
