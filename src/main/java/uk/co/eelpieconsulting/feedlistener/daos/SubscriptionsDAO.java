package uk.co.eelpieconsulting.feedlistener.daos;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Component
public class SubscriptionsDAO {

	private List<Subscription> subscriptions;

	public SubscriptionsDAO() {
		subscriptions = Lists.newArrayList();	
	}
	
	public void add(Subscription subscription) {
		subscriptions.add(subscription);
	}
	
	public List<Subscription> getSubscriptions() {
		return ImmutableList.copyOf(subscriptions);
	}
	
}
