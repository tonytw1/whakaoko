package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.stream.FileImageInputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;

import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscripton;

public class InstagramSubscriptionCallbackParserTest {

	@Test
	public void canParseSubscriptionCallbacksToFindOutWhatsBeenUpdated() throws Exception {
		final InstagramSubscriptionCallbackParser parser = new InstagramSubscriptionCallbackParser();
		final String json = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("instagramSubscriptionCallback.json"));		

		final List<InstagramSubscripton> subscriptions = parser.parse(json);
		
		assertEquals(1, subscriptions.size());
		assertEquals("tag", subscriptions.get(0).getObject());
		assertEquals("nofilter", subscriptions.get(0).getObjectId());
	}

}
