package uk.co.eelpieconsulting.feedlistener.model;

import java.util.List;

public class FeedItemsResult {

    private final List<FeedItem> feedsItems;
    private final long totalCount;

    public FeedItemsResult(List<FeedItem> feedsItems, long totalCount) {
        this.feedsItems = feedsItems;
        this.totalCount = totalCount;
    }

    public List<FeedItem> getFeedsItems() {
        return feedsItems;
    }

    public long getTotalCount() {
        return totalCount;
    }

}
