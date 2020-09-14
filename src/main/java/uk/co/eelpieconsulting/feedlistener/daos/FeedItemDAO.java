package uk.co.eelpieconsulting.feedlistener.daos;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.MongoException;
import dev.morphia.DeleteOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.feedlistener.annotations.Timed;
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator;
import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class FeedItemDAO {

    private static final Sort[] DATE_DESCENDING_THEN_ID = {Sort.descending("date"), Sort.ascending("_id")};
    private static final int MAX_FEED_ITEMS = 25;

    private static Logger log = Logger.getLogger(FeedItemDAO.class);

    private final DataStoreFactory dataStoreFactory;
    private final SubscriptionsDAO subscriptionsDAO;

    @Autowired
    public FeedItemDAO(DataStoreFactory dataStoreFactory, SubscriptionsDAO subscriptionsDAO) {
        this.dataStoreFactory = dataStoreFactory;
        this.subscriptionsDAO = subscriptionsDAO;
    }

    public boolean add(FeedItem feedItem) {
        try {
            if (dataStoreFactory.getDs().find(FeedItem.class).filter(Filters.eq("url", feedItem.getUrl())).iterator().toList().isEmpty()) {    // TODO shouldn't need to read before every write - use an upsert?
                log.info("Added: " + feedItem.getSubscriptionId() + ", " + feedItem.getTitle());
                dataStoreFactory.getDs().save(feedItem);
                return true;
            } else {
                log.debug("Skipping previously added: " + feedItem.getTitle());
                return false;
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

    public FeedItemsResult getSubscriptionFeedItems(Subscription subscription, Integer page) {
        if (page != null) {
            return getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS, page);
        } else {
            return getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS);
        }
    }

    public FeedItemsResult getChannelFeedItemsResult(String username, Channel channel, Integer page, String q, Integer pageSize) {
        final int pageSizeToUse = pageSize != null ? pageSize : MAX_FEED_ITEMS;
        final int pageToUse = (page != null && page > 0) ? page : 1;
        if (pageSizeToUse > MAX_FEED_ITEMS) {
            throw new RuntimeException("Too many records requested");    // TODO use correct exception.
        }

        return !Strings.isNullOrEmpty(q) ? searchChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username, q) :
                getChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username);
    }

    private FeedItemsResult getSubscriptionFeedItems(String subscriptionId, int pageSize) throws MongoException {
        return getSubscriptionFeedItems(subscriptionId, pageSize, 0);
    }
    private FeedItemsResult getSubscriptionFeedItems(String subscriptionId, int pageSize, int page) throws MongoException {
        Query<FeedItem> query = subscriptionFeedItemsQuery(subscriptionId);
        long totalItems = query.count();
        return new FeedItemsResult(query.iterator(withPaginationFor(pageSize, page).sort(DATE_DESCENDING_THEN_ID)).toList(), totalItems);
    }

    public void deleteSubscriptionFeedItems(Subscription subscription) throws MongoException {
        dataStoreFactory.getDs().find(FeedItem.class).filter(Filters.eq("subscriptionId", subscription.getId())).delete(new DeleteOptions().multi(true));
    }

    @Timed(timingNotes = "")
    public long getSubscriptionFeedItemsCount(String subscriptionId) {
        return subscriptionFeedItemsQuery(subscriptionId).count();
    }

    @Timed(timingNotes = "")
    public FeedItemsResult getChannelFeedItems(String channelId, int pageSize, int page, String username) throws MongoException {
        Query<FeedItem> query = channelFeedItemsQuery(username, channelId);
        long totalCount = query.count();
        return new FeedItemsResult(query.iterator(withPaginationFor(pageSize, page).sort(DATE_DESCENDING_THEN_ID)).toList(), totalCount);
    }

    @Timed(timingNotes = "")
    public FeedItemsResult searchChannelFeedItems(String channelId, int pageSize, int page, String username, String q) {
        Query<FeedItem> query = channelFeedItemsQuery(username, channelId).filter(Filters.eq("title", Pattern.compile(q))); // TODO can eq be used with a patten?
        return new FeedItemsResult(query.iterator(withPaginationFor(pageSize, page)).toList(), query.count());
    }

    @Timed(timingNotes = "")
    private Query<FeedItem> channelFeedItemsQuery(String username, String channelId) {
        final List<String> channelSubscriptionIds = Lists.newArrayList();
        for (Subscription subscription : subscriptionsDAO.getSubscriptionsForChannel(username, channelId, null)) {
            channelSubscriptionIds.add(subscription.getId());
        }
        return dataStoreFactory.getDs().find(FeedItem.class).filter(Filters.in("subscriptionId", channelSubscriptionIds));
    }

    private Query<FeedItem> subscriptionFeedItemsQuery(String subscriptionId) {
        return dataStoreFactory.getDs().find(FeedItem.class).filter(Filters.eq("subscriptionId", subscriptionId));
    }

    private FindOptions withPaginationFor(int pageSize, int page) {
        return new FindOptions().limit(pageSize).skip(calculatePageOffset(pageSize, page));
    }

    private int calculatePageOffset(int pageSize, int page) {
        if (page > 0) {
            return (page - 1) * pageSize;
        }
        return 0;
    }

}
