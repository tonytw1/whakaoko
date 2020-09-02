package uk.co.eelpieconsulting.feedlistener.daos;

import com.google.common.base.Strings;
import com.mongodb.MongoException;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.User;

import java.net.UnknownHostException;
import java.util.List;

@Component
public class UsersDAO {

    private static final Sort USERNAME_ASCENDING = Sort.ascending("username");

    private final DataStoreFactory dataStoreFactory;

    @Autowired
    public UsersDAO(DataStoreFactory dataStoreFactory) {
        this.dataStoreFactory = dataStoreFactory;
    }

    public List<User> getUsers() {
        try {
            return dataStoreFactory.getDs().find(User.class).iterator(new FindOptions().sort(USERNAME_ASCENDING)).toList();
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    public User getByUsername(String username) throws UnknownUserException {
        User user = loadUserFromDatabase(username);
        if (user != null) {
            return user;
        }
        throw new UnknownUserException();
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
            return dataStoreFactory.getDs().find(User.class).filter(Filters.eq("username", username)).first();
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

}
