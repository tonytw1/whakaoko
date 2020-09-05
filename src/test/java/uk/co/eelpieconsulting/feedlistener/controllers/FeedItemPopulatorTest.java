package uk.co.eelpieconsulting.feedlistener.controllers;

import org.junit.Test;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import static org.junit.Assert.assertEquals;

public class FeedItemPopulatorTest {

    @Test
    public void canCorrectForExcessivelyEscapedInputFeeds() {
        final String incorrectlyEscapedHeadline = "St John&#39;s Bar, 5 Cable Street, Te Aro, Wellington";
        FeedItem feedItem = new FeedItem(incorrectlyEscapedHeadline, null, null, null, null, null, null);

        FeedItem fixed = new FeedItemPopulator(null).overlyUnescape(feedItem);

        assertEquals("St John's Bar, 5 Cable Street, Te Aro, Wellington", fixed.getHeadline());
    }

}
