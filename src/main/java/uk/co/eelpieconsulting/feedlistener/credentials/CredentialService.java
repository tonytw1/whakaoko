package uk.co.eelpieconsulting.feedlistener.credentials;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.User;

import com.google.common.base.Strings;

@Component
public class CredentialService {
	
	private final String instagramClientId;
	private final String instagramClientSecret;

	private final String twitterConsumerKey;
	private final String twitterConsumerSecret;
	private final UsersDAO usersDAO;
	
	@Autowired
	public CredentialService(UsersDAO usersDAO, @Value("${instagram.client.id}") String instagramClientId,
			@Value("${instagram.client.secret}") String instagramClientSecret,
			@Value("${twitter.consumer.key}") String twitterConsumerKey,
			@Value("${twitter.consumer.secret}") String twitterConsumerSecret) {
		this.usersDAO = usersDAO;
		this.instagramClientId = instagramClientId;
		this.instagramClientSecret = instagramClientSecret;
		this.twitterConsumerKey = twitterConsumerKey;
		this.twitterConsumerSecret = twitterConsumerSecret;
	}
	
	public String getInstagramClientId() {
		return instagramClientId;
	}
	public String getInstagramClientSecret() {
		return instagramClientSecret;
	}
	public String getTwitterConsumerKey() {
		return twitterConsumerKey;
	}
	public String getTwitterConsumerSecret() {
		return twitterConsumerSecret;
	}
	
	public String getTwitterAccessTokenForUser(String username) throws UnknownUserException {
		return usersDAO.getByUsername(username).getTwitterAccessToken();
	}	
	public void setTwitterAccessTokenForUser(String username, String twitterAccessToken) throws UnknownHostException, UnknownUserException {
		final User user = usersDAO.getByUsername(username);
		user.setTwitterAccessToken(twitterAccessToken);
		usersDAO.save(user);
	}
	
	public String getTwitterAccessSecretForUser(String username) throws UnknownUserException {
		return usersDAO.getByUsername(username).getTwitterAccessSecret();
	}
	public void setTwitterAccessSecretForUser(String username, String twitterAccessSecret) throws UnknownHostException, UnknownUserException {
		final User user = usersDAO.getByUsername(username);
		user.setTwitterAccessSecret(twitterAccessSecret);
		usersDAO.save(user);
	}

	public String getInstagramAccessTokenForUser(String username) throws UnknownUserException {
		return usersDAO.getByUsername(username).getInstagramAccessToken();
	}
	public void setInstagramAccessTokenForUser(String username, String accessToken) throws UnknownHostException, UnknownUserException {
		final User user = usersDAO.getByUsername(username);
		user.setInstagramAccessToken(accessToken);
		usersDAO.save(user);		
	}
	
	public boolean hasTwitterAccessToken(String username) {
		try {
			final User user = usersDAO.getByUsername(username);
			return !Strings.isNullOrEmpty(user.getTwitterAccessToken()) && !Strings.isNullOrEmpty(user.getTwitterAccessSecret());
		} catch (UnknownUserException e) {
			return false;
		}
	}
	
	public boolean hasInstagramAccessToken(String username) throws UnknownUserException {
		final User user = usersDAO.getByUsername(username);
		return !Strings.isNullOrEmpty(user.getInstagramAccessToken());
	}
	
}
