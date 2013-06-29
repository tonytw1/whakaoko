package uk.co.eelpieconsulting.feedlistener.instagram;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.instagram.api.InstagramApi;
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

	@Scheduled(fixedDelay=60000 * 60 * 24)	// TODO ideally, only once at startup
	public void resetSubscriptions() throws HttpNotFoundException,
			HttpBadRequestException, HttpForbiddenException,
			HttpFetchException, UnsupportedEncodingException {
		requestUnsubscribeAll(clientId, clientSecret);
		
		log.info("Resubscribing to instagram");
		final List<Subscription> subscriptions = subscriptionsDAO.getSubscriptions();
		for (Subscription subscription : subscriptions) {
			if (subscription.getId().startsWith("instagram/")) {
				requestInstagramTagSubscription(((InstagramTagSubscription) subscription).getTag());
			}
		}
	}
	
	private void requestUnsubscribeAll(String clientId, String clientSecret) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException {
		log.info("Clearing down all active instagram subscriptions");
		instagramApi.deleteAllSubscriptions(clientId, clientSecret);
	}
	
	public void requestInstagramTagSubscription(String tag) throws HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, UnsupportedEncodingException {
		instagramApi.createTagSubscription(tag, clientId, clientSecret, urlBuilder.getInstagramCallbackUrl());
	}
	
}
