package uk.co.eelpieconsulting.feedlistener.model;

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
		return "twitter/tags/" + tag;
	}

	@Override
	public String toString() {
		return "TwitterTagSubscription [tag=" + tag + "]";
	}


	private String generateName(String tag) {
		return "Twitter - " + tag;
	}
	
}
