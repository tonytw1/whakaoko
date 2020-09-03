package uk.co.eelpieconsulting.feedlistener.controllers;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.FeedItemsResult;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedItemPopulator {

    public static int MAX_FEED_ITEMS = 25;

    public void populateFeedItems(FeedItemsResult feedItemsResult, ModelAndView mv, String field) {
        populate(mv, field, feedItemsResult.getFeedsItems());
    }

    private void populate(ModelAndView mv, String field, List<FeedItem> feedItems) {
        // Some source feeds incorrectly over escape entities.
        // In these cases we will import and persist the feed items as provided and HTML unescape when outputting
        // This is repeated processing but makes for a reversible change and an accurate persisted representation of the source feed.
        List<FeedItem> cleanedFeedItems = feedItems.stream().map(this::overlyUnescape).collect(Collectors.toList());
        mv.addObject(field, cleanedFeedItems);
        mv.addObject("geotagged", geoTagged(cleanedFeedItems));
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

    private List<FeedItem> geoTagged(List<FeedItem> feedItems) {
        return feedItems.stream().filter(FeedItem::isGeoTagged).collect(Collectors.toList());
    }

}