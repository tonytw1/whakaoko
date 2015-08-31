package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.User;

import com.google.common.base.Strings;
import com.mongodb.MongoException;

@Component
public class UsersDAO {
	
	private static final String USERNAME = "username";
	
	private final DataStoreFactory dataStoreFactory;

	@Autowired
	public UsersDAO(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}
	
	public List<User> getUsers() {
		try {
			return dataStoreFactory.getDs().find(User.class).
				order(USERNAME).
				asList();
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}

	public User getByUsername(String username) throws UnknownUserException {
        User user = loadUserFromDatabase(username);
        if (user != null) {
        	return user;
        } throw new UnknownUserException();
	}

	public synchronized void createUser(String username) throws UnknownHostException, MongoException {
		if (Strings.isNullOrEmpty(username)) {
			throw new RuntimeException("No username given");
		}
		
		if (loadUserFromDatabase(username) != null) {
			throw new RuntimeException("Username is not available");
		}
		
		save(new User(username));		
	}

	public void save(final User user) throws UnknownHostException {
		dataStoreFactory.getDs().save(user);
	}
	
	private User loadUserFromDatabase(String username) {
		try {
			return dataStoreFactory.getDs().find(User.class, "username", username).get();		
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}
	
}
