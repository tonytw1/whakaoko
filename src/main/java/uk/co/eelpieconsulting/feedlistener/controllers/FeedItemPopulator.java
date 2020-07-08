package uk.co.eelpieconsulting.feedlistener.controllers;

import com.google.common.base.Strings;
import com.mongodb.MongoException;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedItemPopulator {

    private static int MAX_FEED_ITEMS = 25;

    private final FeedItemDAO feedItemDAO;

    @Autowired
    public FeedItemPopulator(FeedItemDAO feedItemDAO) {
        this.feedItemDAO = feedItemDAO;
    }

    public void populateFeedItems(Subscription subscription, Integer page, ModelAndView mv, String field) throws UnknownHostException {
        List<FeedItem> feedItems;
        if (page != null) {
            feedItems = feedItemDAO.getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS, page);
        } else {
            feedItems = feedItemDAO.getSubscriptionFeedItems(subscription.getId(), MAX_FEED_ITEMS);
        }

        populate(mv, field, feedItems);
    }

    public void populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field, String q) throws UnknownHostException, MongoException {
        populateFeedItems(username, channel, page, mv, field, MAX_FEED_ITEMS, q);
    }

    void populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field, Integer pageSize, String q) throws UnknownHostException, MongoException {
        final int pageSizeToUse = pageSize != null ? pageSize : MAX_FEED_ITEMS;
        final int pageToUse = (page != null && page > 0) ? page : 1;
        if (pageSizeToUse > MAX_FEED_ITEMS) {
            throw new RuntimeException("Too many records requested");    // TODO use correct exception.
        }

        List<FeedItem> feedItems = !Strings.isNullOrEmpty(q) ? feedItemDAO.searchChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username, q) :
                feedItemDAO.getChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username);

        // Some source feeds incorrectly over escape entities.
        // In these cases we will import and persist the feed items as provided and HTML unescape when outputting
        // This is repeated processing but makes for a reversible change and an accurate persisted representation of the source feed.
        List<FeedItem> cleanedFeedItems = feedItems.stream().map(this::overlyUnescape).collect(Collectors.toList());

        populate(mv, field, cleanedFeedItems);
    }

    private void populate(ModelAndView mv, String field, List<FeedItem> feedItems) {
        mv.addObject(field, feedItems);
        mv.addObject("geotagged", geoTagged(feedItems));
    }

    private List<FeedItem> geoTagged(List<FeedItem> feedItems) {
        return feedItems.stream().filter(FeedItem::isGeoTagged).collect(Collectors.toList());
    }

    protected FeedItem overlyUnescape(FeedItem feedItem) {
        return new FeedItem(StringEscapeUtils.unescapeHtml(feedItem.getTitle()),
                feedItem.getUrl(),
                StringEscapeUtils.unescapeHtml(feedItem.getBody()),
                feedItem.getDate(),
                feedItem.getPlace(),
                feedItem.getImageUrl(),
                feedItem.getAuthor()
        );
    }

}