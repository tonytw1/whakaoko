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
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedItemPopulator {

    public static int MAX_FEED_ITEMS = 25;

    private final FeedItemDAO feedItemDAO;

    @Autowired
    public FeedItemPopulator(FeedItemDAO feedItemDAO) {
        this.feedItemDAO = feedItemDAO;
    }

    public void populateFeedItems(FeedItemsResult feedItemsResult, ModelAndView mv, String field) {
        populate(mv, field, feedItemsResult.getFeedsItems());
    }

    public long populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field, String q) throws MongoException {
        return populateFeedItems(username, channel, page, mv, field, MAX_FEED_ITEMS, q);
    }

    long populateFeedItems(String username, Channel channel, Integer page, ModelAndView mv, String field, Integer pageSize, String q) throws MongoException {
        final int pageSizeToUse = pageSize != null ? pageSize : MAX_FEED_ITEMS;
        final int pageToUse = (page != null && page > 0) ? page : 1;
        if (pageSizeToUse > MAX_FEED_ITEMS) {
            throw new RuntimeException("Too many records requested");    // TODO use correct exception.
        }

        FeedItemsResult results = !Strings.isNullOrEmpty(q) ? feedItemDAO.searchChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username, q) :
                feedItemDAO.getChannelFeedItems(channel.getId(), pageSizeToUse, pageToUse, username);

        populate(mv, field, results.getFeedsItems());
        return results.getTotalCount();
    }

    private void populate(ModelAndView mv, String field, List<FeedItem> feedItems) {
        // Some source feeds incorrectly over escape entities.
        // In these cases we will import and persist the feed items as provided and HTML unescape when outputting
        // This is repeated processing but makes for a reversible change and an accurate persisted representation of the source feed.
        List<FeedItem> cleanedFeedItems = feedItems.stream().map(this::overlyUnescape).collect(Collectors.toList());

        mv.addObject(field, cleanedFeedItems);
        mv.addObject("geotagged", geoTagged(cleanedFeedItems));
    }

    private List<FeedItem> geoTagged(List<FeedItem> feedItems) {
        return feedItems.stream().filter(FeedItem::isGeoTagged).collect(Collectors.toList());
    }

    protected FeedItem overlyUnescape(FeedItem feedItem) {
        FeedItem fixed = new FeedItem(StringEscapeUtils.unescapeHtml(feedItem.getTitle()),
                feedItem.getUrl(),
                StringEscapeUtils.unescapeHtml(feedItem.getBody()),
                feedItem.getDate(),
                feedItem.getPlace(),
                feedItem.getImageUrl(),
                feedItem.getAuthor()
        );
        fixed.setSubscriptionId(feedItem.getSubscriptionId());
        return fixed;
    }

}