package uk.co.eelpieconsulting.feedlistener.instagram.auth;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.instagram.api.InstagramApi;

@Controller
public class InstagramOauthController {
	
	private static Logger log = Logger.getLogger(InstagramOauthController.class);
	
	private static final String INSTAGRAM_OAUTH_CALLBACK = "/instagram/oauth";
	
	private final UrlBuilder urlBuilder;
	private final InstagramApi instagramApi;
	private final CredentialService credentialService;
	
	@Autowired
	public InstagramOauthController(UrlBuilder urlBuilder, CredentialService credentialService) {
		this.urlBuilder = urlBuilder;
		this.credentialService = credentialService;
		this.instagramApi = new InstagramApi();
	}
	
	@RequestMapping(value="/instagram/authorise", method=RequestMethod.GET)
	public ModelAndView authorize() {
		final String authorizeRedirectUrl = authorizeRedirectUrl();
		log.info("Redirecting user to instagram: " + authorizeRedirectUrl);
		return new ModelAndView(new RedirectView(authorizeRedirectUrl));
	}
	
	@RequestMapping(value=INSTAGRAM_OAUTH_CALLBACK, method=RequestMethod.GET)
	public ModelAndView dataCallback(@RequestParam String code) throws IOException, HttpNotFoundException, HttpBadRequestException, HttpForbiddenException, HttpFetchException, JSONException {		
		log.info("Received oauth callback code: " + code);
		
		log.info("Exchanging oauth callback code for access token");
		final String authorizeRedirectUrl = authorizeRedirectReturnUrl();
		log.info(authorizeRedirectUrl);
		try {
			final String accessToken = instagramApi.getAccessToken(credentialService.getInstagramClientId(), credentialService.getInstagramClientSecret(), code, authorizeRedirectUrl);
			
			log.info("Got instagram access token: " + accessToken);
			credentialService.setInstagramAccessToken(accessToken);
			
		} catch (HttpBadRequestException e) {
			log.error(e.getResponseBody());
		}
		return null;
	}
	
	private String authorizeRedirectUrl() {
		return instagramApi.getAuthorizeRedirectUrl(credentialService.getInstagramClientId(), authorizeRedirectReturnUrl());
	}

	private String authorizeRedirectReturnUrl() {
		return urlBuilder.getBaseUrl() + INSTAGRAM_OAUTH_CALLBACK;
	}
	
}
