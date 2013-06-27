package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscripton;

import com.google.common.collect.Lists;

@Component
public class InstagramSubscriptionCallbackParser {
	
	private static Logger log = Logger.getLogger(InstagramSubscriptionCallbackParser.class);
	
	public List<InstagramSubscripton> parse(String json) throws JSONException {
		List<InstagramSubscripton> subscriptions = Lists.newArrayList();
		
		JSONArray callbackDataJSON = new JSONArray(json);
		log.info("Callback contains subscriptions: " + callbackDataJSON.length());
		for (int i = 0; i < callbackDataJSON.length(); i++) {
			final JSONObject subscriptionJSON = callbackDataJSON.getJSONObject(i);			
			subscriptions.add(new InstagramSubscripton( subscriptionJSON.getString("object"), subscriptionJSON.getString("object_id")));
		}
		return subscriptions;
	}

}
