package uk.co.eelpieconsulting.feedlistener.model;

public abstract class InstagramSubscription extends Subscription {

	protected long subscriptionId;
	
	public final long getSubscriptionId() {
		return subscriptionId;
	}

	public final void setSubscriptionId(long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	
}
