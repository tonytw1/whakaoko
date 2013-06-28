package uk.co.eelpieconsulting.feedlistener.model;

public class InstagramTagSubscription extends Subscription {

	private String tag;
	
	public InstagramTagSubscription() {
	}

	public InstagramTagSubscription(String tag) {
		this.tag = tag;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
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
