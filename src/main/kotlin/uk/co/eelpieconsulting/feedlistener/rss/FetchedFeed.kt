package uk.co.eelpieconsulting.feedlistener.rss

import uk.co.eelpieconsulting.feedlistener.model.FeedItem

class FetchedFeed(val feedName: String?, val  etag: String? = null, val feedItems: List<FeedItem>)