package uk.co.eelpieconsulting.feedlistener.daos;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

@Component
public class FeedItemDAO {
	
	private Map<String, FeedItem> feedItems;
	
	final Function<FeedItem, Date> dateDescending = new Function<FeedItem, Date>() {
		@Override
		public Date apply(FeedItem from) {
			return from.getDate();
		}
	};
	
	final Ordering<FeedItem> dateOrdering = Ordering.natural().onResultOf(dateDescending);
	
	@Autowired
	public FeedItemDAO() {
		this.feedItems = Maps.newHashMap();
	}
	
	public void add(FeedItem feedItem) {
		if (!feedItems.containsKey(feedItem.getId())) {
			feedItems.put(feedItem.getId(), feedItem);
		}
	}
	
	public ImmutableList<FeedItem> getAll() {
		return dateOrdering.immutableSortedCopy(feedItems.values()).reverse();		
	}
	
}
