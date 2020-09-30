package uk.co.eelpieconsulting.feedlistener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.User;

@Component
public class UrlBuilder {

    @Value("${base.url}")
	private String baseUrl;

	public String getBaseUrl() {
		return baseUrl;
	}
	
	public String getInstagramCallbackUrl() {
		return getBaseUrl() + "/instagram/callback";
	}
	
	public String getTwitterCallback(String username) {
		return getBaseUrl() + "/twitter/callback/" + username;
	}
	
	public String getSubscriptionUrl(Subscription subscription) {
		return getBaseUrl() + "/ui/subscriptions/" + subscription.getId();
	}
	
	public String getSubscriptionUrl(String subscriptionId) {
		return getBaseUrl() + "/ui/subscriptions/" + subscriptionId;
	}
	
	public String getDeleteSubscriptionUrl(Subscription subscription) {
		return getBaseUrl() + "/subscriptions/" + subscription.getId() + "/delete";
	}

	public String getReadSubscriptionUrl(Subscription subscription) {
		return getBaseUrl() + "/" + subscription.getUsername() + "/subscriptions/" + subscription.getId() + "/read";
	}
	
	public String getSubscriptionItemsUrl(String subscriptionId) {
		return getBaseUrl() + "/subscriptions/" + subscriptionId;
	}
	
	public String getChannelItemsUrl(String channelId) {
		return getBaseUrl() + "/channels/" + channelId;
	}
	
	public String getChannelUrl(Channel channel) {
		return getBaseUrl() + "/ui/channels/" + channel.getId();
	}

	public String getUserUrl() {
		return getBaseUrl() + "/ui";
	}
	
	public String getNewChannelUrl() {
		return getBaseUrl() + "/ui/channels/new";
	}
	
}
