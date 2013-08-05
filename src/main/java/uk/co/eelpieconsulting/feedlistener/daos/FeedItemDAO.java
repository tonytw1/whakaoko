package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.code.morphia.query.Query;
import com.google.common.collect.Lists;
import com.mongodb.MongoException;

@Component
public class FeedItemDAO {
	
	private static final String DATE_DESCENDING = "-date";

	private static Logger log = Logger.getLogger(FeedItemDAO.class);
	
	private final DataStoreFactory dataStoreFactory;
	private final SubscriptionsDAO subscriptionsDAO;
	
	@Autowired
	public FeedItemDAO(DataStoreFactory dataStoreFactory, SubscriptionsDAO subscriptionsDAO) {
		this.dataStoreFactory = dataStoreFactory;
		this.subscriptionsDAO = subscriptionsDAO;
	}
	
	public void add(FeedItem feedItem) throws UnknownHostException, MongoException {
		if (dataStoreFactory.getDatastore().find(FeedItem.class, "url", feedItem.getUrl()).asList().isEmpty()) {	// TODO
			log.info("Added: " + feedItem.getSubscriptionId() + ", " + feedItem.getTitle());
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
	
	public List<FeedItem> getSubscriptionFeedItems(String subscriptionId, int limit) throws UnknownHostException, MongoException {
		return subscriptionFeedItemsQuery(subscriptionId).limit(limit).asList();
	}
	
	public List<FeedItem> getSubscriptionFeedItems(String subscriptionId, int limit, Integer page) throws UnknownHostException, MongoException {
		return subscriptionFeedItemsQuery(subscriptionId).limit(limit).offset(page * limit).asList();
	}
	
	public long getAllCount() throws UnknownHostException, MongoException {
		return dataStoreFactory.getDatastore().find(FeedItem.class).countAll();
	}
	
	public long getSubscriptionFeedItemsCount(String subscriptionId) throws UnknownHostException {
		return subscriptionFeedItemsQuery(subscriptionId).countAll();
	}
	
	public long getChannelFeedItemsCount(String channelId) throws UnknownHostException, MongoException {
		return channelFeedItemsQuery(channelId).countAll();
	}
	
	public List<FeedItem> getChannelFeedItems(String channelId, int limit) throws UnknownHostException, MongoException {		
		return channelFeedItemsQuery(channelId).limit(limit).asList();
	}
	
	public List<FeedItem> getChannelFeedItems(String channelId, int limit, Integer page) throws UnknownHostException, MongoException {
		return channelFeedItemsQuery(channelId).limit(limit).offset(page * limit).asList();
	}
	
	private Query<FeedItem> channelFeedItemsQuery(String channelId) throws UnknownHostException {
		final List<String> channelSubscriptions = Lists.newArrayList();
		for (Subscription subscription : subscriptionsDAO.getSubscriptionsForChannel(channelId)) {
			channelSubscriptions.add(subscription.getId());
		}		
		return dataStoreFactory.getDatastore().find(FeedItem.class).field("subscriptionId").hasAnyOf(channelSubscriptions).order(DATE_DESCENDING);
	}
	
	private Query<FeedItem> inboxQuery() throws UnknownHostException {
		return dataStoreFactory.getDatastore().find(FeedItem.class).order(DATE_DESCENDING);
	}

	private Query<FeedItem> subscriptionFeedItemsQuery(String subscriptionId) throws UnknownHostException {
		return dataStoreFactory.getDatastore().find(FeedItem.class, "subscriptionId", subscriptionId).order(DATE_DESCENDING);
	}
	
}
