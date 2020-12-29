package uk.co.eelpieconsulting.feedlistener;

import org.jetbrains.annotations.NotNull;
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
        return subscriptionUrl(subscription) + "/read";
    }

    public String getSubscriptionItemsUrl(Subscription subscription) {
        return subscriptionUrl(subscription) + "/items";
    }

    public String getChannelItemsUrl(Channel channel) {
        return getBaseUrl() + "/channels/" + channel.id + "/items";
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

    private String subscriptionUrl(Subscription subscription) {
        return getBaseUrl() + "/subscriptions/" + subscription.getId();
    }
}
