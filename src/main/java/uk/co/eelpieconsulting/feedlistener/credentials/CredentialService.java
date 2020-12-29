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

    private final String twitterConsumerKey;
    private final String twitterConsumerSecret;
    private final UsersDAO usersDAO;

    @Autowired
    public CredentialService(UsersDAO usersDAO,
                             @Value("${twitter.consumer.key}") String twitterConsumerKey,
                             @Value("${twitter.consumer.secret}") String twitterConsumerSecret) {
        this.usersDAO = usersDAO;
        this.twitterConsumerKey = twitterConsumerKey;
        this.twitterConsumerSecret = twitterConsumerSecret;
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

    public boolean hasTwitterAccessToken(String username) {
        try {
            final User user = usersDAO.getByUsername(username);
            return !Strings.isNullOrEmpty(user.getTwitterAccessToken()) && !Strings.isNullOrEmpty(user.getTwitterAccessSecret());
        } catch (UnknownUserException e) {
            return false;
        }
    }

}
