package uk.co.eelpieconsulting.feedlistener.model;

public class InstagramTagSubscription extends Subscription {

	private final String tag;

	public InstagramTagSubscription(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}
	
	@Override
	public String getId() {
		return "instagram/tags/" + tag;
	}

	@Override
	public String toString() {
		return "InstagramTagSubscription [tag=" + tag + "]";
	}

	
}
