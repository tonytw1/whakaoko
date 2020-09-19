package uk.co.eelpieconsulting.feedlistener.daos;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.model.*;

@Component
public class DataStoreFactory {

    private static final Logger log = Logger.getLogger(DataStoreFactory.class);

    private final String mongoUri;
    private final String mongoDatabase;

    private Datastore datastore;

    @Autowired
    public DataStoreFactory(@Value("${mongo.uri}") String mongoUri,
                            @Value("${mongo.database}") String mongoDatabase
                            ) throws MongoException {
        this.mongoUri = mongoUri;
        this.mongoDatabase = mongoDatabase;
    }

    public Datastore getDs() {
        if (datastore == null) {
            datastore = createDataStore(mongoUri, mongoDatabase);
            datastore.ensureIndexes();
        }
        return datastore;
    }

    private Datastore createDataStore(String mongoUri, String database) {

        try {
            //MongoClient m = credential != null ? new MongoClient(serverAddresses, credential, mongoClientOptions) : new MongoClient(serverAddresses, mongoClientOptions);

            MapperOptions mapperOptions = MapperOptions.builder().
                    discriminatorKey("className").discriminator(DiscriminatorFunction.className()).
                    build();

            MongoClient mongoClient = MongoClients.create(mongoUri);
            Datastore datastore = Morphia.createDatastore(mongoClient, database, mapperOptions);

            // These explicit mappings are needed to trigger subclass querying during finds;
            // see subtype in LegacyQuery source code for hints
            datastore.getMapper().map(InstagramSubscription.class);
            datastore.getMapper().map(InstagramGeographySubscription.class);
            datastore.getMapper().map(InstagramTagSubscription.class);
            datastore.getMapper().map(TwitterTagSubscription.class);
            datastore.getMapper().map(RssSubscription.class);
            return datastore;

        } catch (MongoException e) {
            log.error(e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}