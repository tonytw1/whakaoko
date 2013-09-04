package uk.co.eelpieconsulting.feedlistener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class UrlBuilder {

    @Value("#{config['base.url']}")
	private String baseUrl;

	public String getBaseUrl() {
		return baseUrl;
	}
	
	public String getInstagramCallbackUrl() {
		return getBaseUrl() + "/instagram/callback";
	}

	public String getSubscriptionUrl(Subscription subscription) {
		return getBaseUrl() + "/ui/" + subscription.getUsername() + "/subscriptions/" + subscription.getId();
	}
	
	public String getChannelUrl(Channel channel) {
		return getBaseUrl() + "/ui/" + channel.getUsername() + "/channels/" + channel.getId();
	}
	
}
