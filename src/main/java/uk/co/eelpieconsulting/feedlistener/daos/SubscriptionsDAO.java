package uk.co.eelpieconsulting.feedlistener.daos;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Component
public class SubscriptionsDAO {

	private List<RssSubscription> subscriptions;

	public SubscriptionsDAO() {
		subscriptions = Lists.newArrayList();
		add(new RssSubscription("http://wellington.gen.nz"));
	}
	
	public void add(RssSubscription subscription) {
		subscriptions.add(subscription);
	}
	
	public List<RssSubscription> getSubscriptions() {
		return ImmutableList.copyOf(subscriptions);
	}
	
}
