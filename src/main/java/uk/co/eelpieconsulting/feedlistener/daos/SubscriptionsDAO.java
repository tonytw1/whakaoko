package uk.co.eelpieconsulting.feedlistener.daos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import uk.co.eelpieconsulting.feedlistener.model.RssSubscription;

@Component
public class SubscriptionsDAO {

	private List<RssSubscription> subscriptions;

	@Autowired
	public SubscriptionsDAO() {
		subscriptions = Lists.newArrayList();
	}
	
	public void addSubscription(RssSubscription subscription) {
		subscriptions.add(subscription);
	}
	
	public List<RssSubscription> getSubscriptions() {
		return ImmutableList.copyOf(subscriptions);
	}
	
}
