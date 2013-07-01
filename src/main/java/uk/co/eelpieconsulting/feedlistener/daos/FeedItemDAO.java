package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException;

@Component
public class FeedItemDAO {
		
	private final DataStoreFactory dataStoreFactory;
	
	@Autowired
	public FeedItemDAO(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}
	
	public void add(FeedItem feedItem) throws UnknownHostException, MongoException {
		dataStoreFactory.getDatastore().save(feedItem);		
	}
	
	public void addAll(List<FeedItem> feedItems) throws UnknownHostException, MongoException {
		dataStoreFactory.getDatastore().save(feedItems);			
	}
	
	public List<FeedItem> getAll() throws UnknownHostException, MongoException {
		return inboxQuery().asList();
	}
	
	public List<FeedItem> getInbox(int limit) throws UnknownHostException, MongoException {
		return inboxQuery().limit(limit).asList();
	}
	
	public long getAllCount() throws UnknownHostException, MongoException {
		return dataStoreFactory.getDatastore().find(FeedItem.class).countAll();
	}
	
	private Query<FeedItem> inboxQuery() throws UnknownHostException {
		return dataStoreFactory.getDatastore().find(FeedItem.class).order("-date");
	}
	
}
