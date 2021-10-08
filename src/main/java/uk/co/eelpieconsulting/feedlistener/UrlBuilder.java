package uk.co.eelpieconsulting.feedlistener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class UrlBuilder {

    @Value("${base.url}")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
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
        return subscriptionUrl(subscription) + "/delete";
    }

    public String getReadSubscriptionUrl(Subscription subscription) {
        return "/ui/subscriptions/" + subscription.id + "/read";
    }

    public String getSubscriptionItemsUrl(Subscription subscription) {
        return subscriptionUrl(subscription) + "/items";
    }

    public String getChannelItemsUrl(Channel channel) {
        return getBaseUrl() + "/channels/" + channel.id + "/items";
    }

    public String getChannelNewSubscriptionUrl(Channel channel) {
        return getBaseUrl() + "/ui/subscriptions/" + channel.id + "/new";
    }

    public String getChannelUrl(Channel channel) {
        return getBaseUrl() + "/ui/channels/" + channel.getId();
    }

    public String getUserUrl() {
        return getBaseUrl() + "/";
    }

    public String getNewChannelUrl() {
        return getBaseUrl() + "/ui/channels/new";
    }

    private String subscriptionUrl(Subscription subscription) {
        return getBaseUrl() + "/subscriptions/" + subscription.getId();
    }
}
