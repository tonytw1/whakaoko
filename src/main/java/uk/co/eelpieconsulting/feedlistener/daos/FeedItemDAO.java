package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException;

@Component
public class FeedItemDAO {
	
	private static Logger log = Logger.getLogger(FeedItemDAO.class);
	
	private final DataStoreFactory dataStoreFactory;
	
	@Autowired
	public FeedItemDAO(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}
	
	public void add(FeedItem feedItem) throws UnknownHostException, MongoException {
		if (dataStoreFactory.getDatastore().find(FeedItem.class, "url", feedItem.getUrl()).asList().isEmpty()) {	// TODO
			log.info("Added: " + feedItem.getTitle());
			dataStoreFactory.getDatastore().save(feedItem);
		}
	}
	
	public void addAll(List<FeedItem> feedItems) throws UnknownHostException, MongoException {
		for (FeedItem feedItem : feedItems) {
			add(feedItem);
		}
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
