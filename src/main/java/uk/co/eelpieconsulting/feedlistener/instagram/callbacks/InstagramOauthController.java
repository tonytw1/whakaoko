package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
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
import uk.co.eelpieconsulting.feedlistener.instagram.api.InstagramApi;

@Controller
public class InstagramOauthController {
	
	private static Logger log = Logger.getLogger(InstagramOauthController.class);
	
	private static final String INSTAGRAM_OAUTH_CALLBACK = "/instagram/oauth";
	
	private final String CLIENT_ID = "";
	private final String CLIENT_SECRET = "";

	private final InstagramApi instagramApi;
	
	public InstagramOauthController() {
		this.instagramApi = new InstagramApi();
	}
	
	@RequestMapping(value="/instagram/authorize", method=RequestMethod.GET)
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
			final String accessToken = instagramApi.getAccessToken(CLIENT_ID, CLIENT_SECRET, code, authorizeRedirectUrl);
			log.info("Got access token: " + accessToken);
			
		} catch (HttpBadRequestException e) {
			log.error(e.getResponseBody());
		}
		return null;
	}
	
	private String authorizeRedirectUrl() {
		return instagramApi.getAuthorizeRedirectUrl(CLIENT_ID, authorizeRedirectReturnUrl());
	}

	private String authorizeRedirectReturnUrl() {
		return "http://genil.eelpieconsulting.co.uk" + INSTAGRAM_OAUTH_CALLBACK;
	}
	
}
