package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

@Component
public class DataStoreFactory {
	
    @Value("#{config['mongo.host']}")
    private String mongoHost;
    
    @Value("#{config['mongo.database']}")
    private String mongoDatabase;

	private Datastore dataStore;
        
	public DataStoreFactory() {
	}

	public DataStoreFactory(String mongoHost, String mongoDatabase) {
		this.mongoHost = mongoHost;
		this.mongoDatabase = mongoDatabase;
	}
	
	public Datastore getDatastore() throws UnknownHostException, MongoException {
		if (dataStore != null) {
			return dataStore;
		}
		
		return connect();
	}

	private synchronized Datastore connect() throws UnknownHostException {
		if (dataStore != null) {
			return dataStore;
		}
		
		final MongoClient client = new MongoClient(mongoHost);
		
		final Morphia morphia = new Morphia();				
		Datastore newDataStore = morphia.createDatastore(client, mongoDatabase);
		morphia.map(FeedItem.class);
		newDataStore.ensureIndexes();
		
		this.dataStore = newDataStore;
		return this.dataStore;
	}
	
}
