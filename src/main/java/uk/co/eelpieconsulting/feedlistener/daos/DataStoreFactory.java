package uk.co.eelpieconsulting.feedlistener.daos;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.*;
import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.User;

import java.net.UnknownHostException;
import java.util.List;

@Component
public class DataStoreFactory {

    private static final Logger log = Logger.getLogger(DataStoreFactory.class);

    private final List<ServerAddress> serverAddresses;
    private final String mongoDatabase;
    private final MongoClientOptions mongoClientOptions;

    private final List<MongoCredential> credentials;

    private Datastore datastore;

    @Autowired
    public DataStoreFactory(@Value("#{config['mongo.host']}") String mongoHost,
                            @Value("#{config['mongo.database']}") String mongoDatabase,
                            @Value("#{config['mongo.user']}") String mongoUser,
                            @Value("#{config['mongo.password']}") String mongoPassword,
                            @Value("#{config['mongo.ssl']}") Boolean mongoSSL) throws UnknownHostException, MongoException {

        List<ServerAddress> addresses = Lists.newArrayList();
        String[] split = mongoHost.split(",");
        for (int i = 0; i < split.length; i++) {
            addresses.add(new ServerAddress(split[i]));
        }
        this.serverAddresses = addresses;

        this.mongoDatabase = mongoDatabase;
        this.mongoClientOptions = MongoClientOptions.builder().sslEnabled(mongoSSL).build();
        this.credentials = !Strings.isNullOrEmpty(mongoUser) ? Lists.newArrayList(MongoCredential.createCredential(mongoUser, mongoDatabase, mongoPassword.toCharArray())) : null;
    }

    public Datastore getDs() {
        if (datastore == null) {
            datastore = createDataStore(mongoDatabase);
            datastore.ensureIndexes();
        }
        return datastore;
    }

    private Datastore createDataStore(String database) {
        Morphia morphia = new Morphia();
        morphia.map(FeedItem.class);
        morphia.map(Subscription.class);
        morphia.map(Channel.class);
        morphia.map(User.class);

        try {
            MongoClient m = credentials != null ? new MongoClient(serverAddresses, credentials, mongoClientOptions) : new MongoClient(serverAddresses, mongoClientOptions);
            return morphia.createDatastore(m, database);

        } catch (MongoException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

}