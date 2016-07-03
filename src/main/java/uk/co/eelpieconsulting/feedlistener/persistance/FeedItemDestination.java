package uk.co.eelpieconsulting.feedlistener.persistance;

import java.util.List;

import uk.co.eelpieconsulting.feedlistener.exceptions.FeeditemPersistanceException;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

public interface FeedItemDestination {

	public void add(FeedItem feedItem) throws FeeditemPersistanceException;
	
	public void addAll(List<FeedItem> feedItems) throws FeeditemPersistanceException;
	
}
