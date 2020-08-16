package uk.co.eelpieconsulting.feedlistener.daos;

import com.google.common.collect.Lists;
import com.mongodb.MongoException;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.annotations.Timed;
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class FeedItemDAO {

    private static final String DATE_DESCENDING = "-date,_id";

    private static Logger log = Logger.getLogger(FeedItemDAO.class);

    private DataStoreFactory dataStoreFactory;
    private SubscriptionsDAO subscriptionsDAO;

    public FeedItemDAO() {
    }

    @Autowired
    public FeedItemDAO(DataStoreFactory dataStoreFactory, SubscriptionsDAO subscriptionsDAO) {
        this.dataStoreFactory = dataStoreFactory;
        this.subscriptionsDAO = subscriptionsDAO;
    }

    public void add(FeedItem feedItem) {
        try {
            if (dataStoreFactory.getDs().find(FeedItem.class, "url", feedItem.getUrl()).asList().isEmpty()) {    // TODO shouldn't need to read before every write - use an upsert?
                log.info("Added: " + feedItem.getSubscriptionId() + ", " + feedItem.getTitle());
                dataStoreFactory.getDs().save(feedItem);
            }
        } catch (Exception e) {
            throw new FeeditemPersistanceException(e);
        }
    }

    public void addAll(List<FeedItem> feedItems) {
        for (FeedItem feedItem : feedItems) {
            add(feedItem);
        }
    }

    public FeedItemsResult getSubscriptionFeedItems(String subscriptionId, int pageSize) throws MongoException {
        return getSubscriptionFeedItems(subscriptionId, pageSize, 0);
    }

    public FeedItemsResult getSubscriptionFeedItems(String subscriptionId, int pageSize, int page) throws MongoException {
        Query<FeedItem> query = subscriptionFeedItemsQuery(subscriptionId);
        FindOptions withPagination = new FindOptions().limit(pageSize).skip(calculatePageOffset(pageSize, page));
        List<FeedItem> feedItems = query.find(withPagination).toList();

        long totalItems = query.count();
        return new FeedItemsResult(feedItems, totalItems);
    }

    public void deleteSubscriptionFeedItems(Subscription subscription) throws MongoException {
        dataStoreFactory.getDs().delete(dataStoreFactory.getDs().find(FeedItem.class, "subscriptionId", subscription.getId()));
    }

    @Timed(timingNotes = "")
    public long getSubscriptionFeedItemsCount(String subscriptionId) {
        return subscriptionFeedItemsQuery(subscriptionId).countAll();
    }

    @Timed(timingNotes = "")
    public FeedItemsResult getChannelFeedItems(String channelId, int pageSize, int page, String username) throws MongoException {
        Query<FeedItem> query = channelFeedItemsQuery(username, channelId);
        long totalCount = query.count();
        FindOptions withPagination = new FindOptions().limit(pageSize).skip(calculatePageOffset(pageSize, page));
        return new FeedItemsResult(query.find(withPagination).toList(), totalCount);
    }

    @Timed(timingNotes = "")
    public FeedItemsResult searchChannelFeedItems(String channelId, int pageSize, int page, String username, String q) {
        Query<FeedItem> query = channelFeedItemsQuery(username, channelId).filter("title", Pattern.compile(q));
        FindOptions withPagination = new FindOptions().limit(pageSize).skip(calculatePageOffset(pageSize, page));
        return new FeedItemsResult(query.find(withPagination).toList(), query.count());
    }

    @Timed(timingNotes = "")
    private Query<FeedItem> channelFeedItemsQuery(String username, String channelId) {
        final List<String> channelSubscriptions = Lists.newArrayList();
        for (Subscription subscription : subscriptionsDAO.getSubscriptionsForChannel(username, channelId, null)) {
            channelSubscriptions.add(subscription.getId());
        }
        return dataStoreFactory.getDs().find(FeedItem.class).field("subscriptionId").hasAnyOf(channelSubscriptions).order(DATE_DESCENDING);
    }

    private Query<FeedItem> subscriptionFeedItemsQuery(String subscriptionId) {
        return dataStoreFactory.getDs().find(FeedItem.class, "subscriptionId", subscriptionId).order(DATE_DESCENDING);
    }

    private int calculatePageOffset(int pageSize, int page) {
        if (page > 0) {
            return (page - 1) * pageSize;
        }
        return 0;
    }

}
