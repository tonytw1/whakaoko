package uk.co.eelpieconsulting.feedlistener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

	public String getSubscriptionUrl(String subscriptionId) {
		return getBaseUrl() + "/subscriptions/" + subscriptionId;
	}

}
