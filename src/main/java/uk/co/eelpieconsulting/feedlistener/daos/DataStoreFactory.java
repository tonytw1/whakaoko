package uk.co.eelpieconsulting.feedlistener.daos;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataStoreFactory {

    private static final Logger log = Logger.getLogger(DataStoreFactory.class);

    private final List<ServerAddress> serverAddresses;
    private final String mongoDatabase;
    private final MongoClientOptions mongoClientOptions;

    private MongoCredential credential;

    private Datastore datastore;

    @Autowired
    public DataStoreFactory(@Value("${mongo.host}") String mongoHost,
                            @Value("${mongoPort}") Integer mongoPort,
                            @Value("${mongo.database}") String mongoDatabase,
                            @Value("${mongo.user}") String mongoUser,
                            @Value("${mongo.password}") String mongoPassword,
                            @Value("${mongo.ssl}") Boolean mongoSSL) throws MongoException {

        List<ServerAddress> addresses = Lists.newArrayList();
        String[] split = mongoHost.split(",");
        for (int i = 0; i < split.length; i++) {
            addresses.add(new ServerAddress(split[i], mongoPort));
        }
        this.serverAddresses = addresses;

        this.mongoDatabase = mongoDatabase;
        this.mongoClientOptions = MongoClientOptions.builder().sslEnabled(mongoSSL).build();
        this.credential = !Strings.isNullOrEmpty(mongoUser) ? MongoCredential.createCredential(mongoUser, mongoDatabase, mongoPassword.toCharArray()) : null;
    }

    public Datastore getDs() {
        if (datastore == null) {
            datastore = createDataStore(mongoDatabase);
            datastore.ensureIndexes();
        }
        return datastore;
    }

    private Datastore createDataStore(String database) {

        try {
            //MongoClient m = credential != null ? new MongoClient(serverAddresses, credential, mongoClientOptions) : new MongoClient(serverAddresses, mongoClientOptions);

            Datastore datastore = Morphia.createDatastore(MongoClients.create(), database);
            return datastore;

            //morphia.map(FeedItem.class);
            //morphia.map(Subscription.class);
            //morphia.map(Channel.class);
            //morphia.map(User.class);


        } catch (MongoException e) {
            log.error(e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}