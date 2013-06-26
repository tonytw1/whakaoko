package uk.co.eelpieconsulting.feedlistener.daos;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

@Component
public class FeedItemDAO {
	
	private static Logger log = Logger.getLogger(FeedItemDAO.class);
	
	private ConcurrentHashMap<String, FeedItem> feedItems;
	
	private final Function<FeedItem, Date> dateDescending = new Function<FeedItem, Date>() {
		@Override
		public Date apply(FeedItem from) {
			return from.getDate();
		}
	};
	
	private final Ordering<FeedItem> dateDescendingOrdering = Ordering.natural().reverse().onResultOf(dateDescending);
	
	@Autowired
	public FeedItemDAO(@Qualifier("feedItemsMap") ConcurrentHashMap<String, FeedItem> feedItems) {
		this.feedItems = feedItems;
	}
	
	public void add(FeedItem feedItem) {
		if (!feedItems.containsKey(feedItem.getId())) {
			log.info("Added; " + feedItem);
			feedItems.put(feedItem.getId(), feedItem);
		}
	}
	
	public ImmutableList<FeedItem> getAll() {
		return dateDescendingOrdering.immutableSortedCopy(feedItems.values());		
	}
	
}
