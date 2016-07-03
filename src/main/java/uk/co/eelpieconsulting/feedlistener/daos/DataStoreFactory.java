package uk.co.eelpieconsulting.feedlistener.daos;

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
	    
	private final ServerAddress serverAddress;
	private final String mongoDatabase;
	private final MongoClientOptions mongoClientOptions;
	
	private final List<MongoCredential> credentials;

	private Datastore datastore;

	@Autowired
	public DataStoreFactory(@Value("#{config['mongoHost']}") String mongoHost,
			@Value("#{config['mongoDatabase']}") String mongoDatabase,
			@Value("#{config['mongoUser']}") String mongoUser,
			@Value("#{config['mongoPassword']}") String mongoPassword,
			@Value("#{config['mongoSSL']}") Boolean mongoSSL) throws UnknownHostException, MongoException {
		this.mongoDatabase = mongoDatabase;

		MongoCredential createCredential = MongoCredential.createCredential(mongoUser, mongoDatabase, mongoPassword.toCharArray());
		this.credentials = Lists.newArrayList(createCredential);

		this.mongoClientOptions = MongoClientOptions.builder().sslEnabled(mongoSSL).build();
		this.serverAddress = new ServerAddress(mongoHost);
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
			MongoClient m = new MongoClient(serverAddress, credentials, mongoClientOptions);
			return morphia.createDatastore(m, database);
			
		} catch (MongoException e) {
			log.error(e);
			throw new RuntimeException(e);
		}		
	}
	
}