package uk.co.eelpieconsulting.feedlistener.instagram;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.feedlistener.CredentialsRequiredException;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.InstagramGeographySubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramTagSubscription;
import uk.co.eelpieconsulting.instagram.api.InstagramApi;

@Component
public class InstagramSubscriptionManager {

    private static Logger log = Logger.getLogger(InstagramSubscriptionManager.class);

    private final UrlBuilder urlBuilder;
    private final CredentialService credentialService;
    private final InstagramApi instagramApi;

    @Autowired
    public InstagramSubscriptionManager(UrlBuilder urlBuilder, CredentialService credentialService, InstagramApi instagramApi) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnsupportedEncodingException {
        this.urlBuilder = urlBuilder;
        this.credentialService = credentialService;
        this.instagramApi = instagramApi;
    }

    public InstagramTagSubscription requestInstagramTagSubscription(String tag, String channelId, String username) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnsupportedEncodingException, JSONException, UnknownUserException {
        if (!credentialService.hasInstagramAccessToken(username)) {
            log.info("No instagram credentials available; not requesting subscription");
            throw new CredentialsRequiredException();
        }

        final InstagramTagSubscription subscription = instagramApi.createTagSubscription(tag, credentialService.getInstagramClientId(), credentialService.getInstagramClientSecret(), urlBuilder.getInstagramCallbackUrl(), channelId, username);
        log.info("Subscribed to instagram tag '" + tag + ": " + subscription);
        return subscription;
    }

    public InstagramGeographySubscription requestInstagramGeographySubscription(LatLong latLong, int radius, String channelId, String username) throws UnsupportedEncodingException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException, UnknownUserException {
        if (!credentialService.hasInstagramAccessToken(username)) {
            log.info("No instagram credentials available; not requesting subscription");
            throw new CredentialsRequiredException();
        }

        InstagramGeographySubscription subscription = instagramApi.createGeographySubscription(latLong, radius, credentialService.getInstagramClientId(), credentialService.getInstagramClientSecret(), urlBuilder.getInstagramCallbackUrl(), channelId, username);
        log.info("Subscribed to instagram geography: " + subscription);
        return subscription;
    }

    public void requestUnsubscribeFrom(long id) throws HttpFetchException {
        log.info("Unsubscribing from instagram: " + id);
        instagramApi.deleteSubscription(id, credentialService.getInstagramClientId(), credentialService.getInstagramClientSecret());
    }

}
