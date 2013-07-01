package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class InstagramSubscriptionCallbackParserTest {

	@Test
	public void canParseSubscriptionCallbacksToFindOutWhatsBeenUpdated() throws Exception {
		final InstagramSubscriptionCallbackParser parser = new InstagramSubscriptionCallbackParser();
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("instagramSubscriptionCallback.json"));		

		final List<Long> subscriptions = parser.parse(json);
		
		assertEquals(1, subscriptions.size());
		final long subscrptionId = subscriptions.get(0);
		assertEquals(3459542, subscrptionId);
	}

}
