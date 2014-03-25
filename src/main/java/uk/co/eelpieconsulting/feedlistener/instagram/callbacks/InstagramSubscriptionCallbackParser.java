package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class InstagramSubscriptionCallbackParser {
	
	private static Logger log = Logger.getLogger(InstagramSubscriptionCallbackParser.class);
	
	public List<Long> parse(String json) throws JSONException {
		log.debug("Subscription update callback: " + json);
		JSONArray callbackDataJSON = new JSONArray(json);
		log.debug("Callback contains subscriptions: " + callbackDataJSON.length());
		
		final List<Long> subscriptions = Lists.newArrayList();
		for (int i = 0; i < callbackDataJSON.length(); i++) {
			final JSONObject subscriptionJSON = callbackDataJSON.getJSONObject(i);			
			subscriptions.add(subscriptionJSON.getLong("subscription_id"));
		}
		return subscriptions;
	}

}
