package uk.co.eelpieconsulting.feedlistener.controllers;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.eelpieconsulting.feedlistener.controllers.ui.SubscriptionLabelService;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;

import static org.junit.Assert.assertEquals;

public class FeedItemPopulatorTest {

    private SubscriptionLabelService subscriptionLabelService = Mockito.mock(SubscriptionLabelService.class);

    @Test
    public void canCorrectForExcessivelyEscapedInputFeeds() {
        final String incorrectlyEscapedHeadline = "St John&#39;s Bar, 5 Cable Street, Te Aro, Wellington";
        FeedItem feedItem = new FeedItem(incorrectlyEscapedHeadline, null, null, DateTime.now().toDate(), null, null, null, null, null);

        FeedItem fixed = new FeedItemPopulator(subscriptionLabelService).overlyUnescape(feedItem);

        assertEquals("St John's Bar, 5 Cable Street, Te Aro, Wellington", fixed.getHeadline());
    }

}
