package uk.co.eelpieconsulting.feedlistener.instagram;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.instagram.api.InstagramApi;
import uk.co.eelpieconsulting.feedlistener.model.InstagramGeographySubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramSubscription;
import uk.co.eelpieconsulting.feedlistener.model.InstagramTagSubscription;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class InstagramSubscriptionManager {
	
	private static Logger log = Logger.getLogger(InstagramSubscriptionManager.class);
	
	private final SubscriptionsDAO subscriptionsDAO;
	private final UrlBuilder urlBuilder;

	private final InstagramApi instagramApi;
	
	private final String clientId;
	private final String clientSecret;
	
	@Autowired
	public InstagramSubscriptionManager(SubscriptionsDAO subscriptionsDAO, UrlBuilder urlBuilder,
			@Value("#{config['instagram.client.id']}") String clientId,
			@Value("#{config['instagram.client.secret']}") String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnsupportedEncodingException {
		this.subscriptionsDAO = subscriptionsDAO;
		this.urlBuilder = urlBuilder;
		this.clientId = clientId;
		this.clientSecret = clientSecret;		
		this.instagramApi = new InstagramApi();		
	}

	//@Scheduled(fixedDelay=60000 * 60 * 24)	// TODO ideally, only once at startup
	public void resetSubscriptions() throws HttpNotFoundException,
			HttpBadRequestException, HttpForbiddenException,
			HttpFetchException, UnsupportedEncodingException, JSONException {
		requestUnsubscribeAll(clientId, clientSecret);
		
		log.info("Resubscribing to instagram");
		final List<Subscription> subscriptions = subscriptionsDAO.getSubscriptions();
		for (Subscription subscription : subscriptions) {
			if (subscription.getId().startsWith("instagram")) {
				if (subscription instanceof InstagramTagSubscription) {
					requestInstagramTagSubscription(((InstagramTagSubscription) subscription).getTag());
				}
				if (subscription instanceof InstagramGeographySubscription) {
					InstagramGeographySubscription instagramGeographySubscription = requestInstagramGeographySubscription(((InstagramGeographySubscription) subscription).getLatLong(), ((InstagramGeographySubscription) subscription).getRadius());
					((InstagramSubscription) subscription).setSubscriptionId(instagramGeographySubscription.getSubscriptionId());
					((InstagramGeographySubscription) subscription).setGeoId(instagramGeographySubscription.getGeoId());
					subscriptionsDAO.save(subscription);
				}

			}
		}
	}
	
	private void requestUnsubscribeAll(String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {
		log.info("Clearing down all active instagram subscriptions");
		instagramApi.deleteAllSubscriptions(clientId, clientSecret);
	}
	
	public InstagramTagSubscription requestInstagramTagSubscription(String tag) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnsupportedEncodingException, JSONException {
		final InstagramTagSubscription subscription = instagramApi.createTagSubscription(tag, clientId, clientSecret, urlBuilder.getInstagramCallbackUrl());
		log.info("Subscribed to instagram tag '" + tag + ": " + subscription);
		return subscription;
	}

	public InstagramGeographySubscription requestInstagramGeographySubscription(LatLong latLong, int radius) throws UnsupportedEncodingException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException {
		InstagramGeographySubscription subscription = instagramApi.createGeographySubscription(latLong, radius, clientId, clientSecret, urlBuilder.getInstagramCallbackUrl());
		log.info("Subscribed to instagram geography: " + subscription);
		return subscription;
	}

	public void requestUnsubscribeFrom(long id) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {
		log.info("Unsubscribing from instagram: " + id);
		instagramApi.deleteSubscription(id, clientId, clientSecret);		
	}
	
}
