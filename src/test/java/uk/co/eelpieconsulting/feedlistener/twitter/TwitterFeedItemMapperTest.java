package uk.co.eelpieconsulting.feedlistener.twitter;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.User;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TwitterFeedItemMapperTest {

    private static Status status = mock(Status.class);
    private static User user = mock(User.class);

    private TwitterFeedItemMapper twitterFeedItemMapper = new TwitterFeedItemMapper();
    private TwitterTagSubscription twitterSubscription = new TwitterTagSubscription(UUID.randomUUID().toString(), "", "");

    @BeforeAll
    public static void setup() {
        MediaEntity[] array = new MediaEntity[0];
        when(status.getMediaEntities()).thenReturn(array);
        when(user.getScreenName()).thenReturn("atwitteruser");
    }

    @Test
    public void canExtractTweetTextAsTitle() throws Exception {
        when(status.getUser()).thenReturn(user).thenReturn(user);
        when(status.getText()).thenReturn("Upto 140 characters of stuff");
        when(status.getCreatedAt()).thenReturn(DateTime.now().toDate());

        FeedItem feedItem = new TwitterFeedItemMapper().createFeedItemFrom(status, twitterSubscription);

        assertEquals("Upto 140 characters of stuff", feedItem.getTitle());
    }

    @Test
    public void canInferWebUrlOfTweet() throws Exception {
        when(status.getId()).thenReturn(439216399400304640L);
        when(user.getScreenName()).thenReturn("RowtownCmmnty");
        when(status.getUser()).thenReturn(user).thenReturn(user);
        when(status.getCreatedAt()).thenReturn(DateTime.now().toDate());

        FeedItem feedItem = new TwitterFeedItemMapper().createFeedItemFrom(status, twitterSubscription);

        assertEquals("https://twitter.com/RowtownCmmnty/status/439216399400304640", feedItem.getUrl());
    }

    @Test
    public void canExtractUserScreenNameFromTweet() throws Exception {
        when(status.getUser()).thenReturn(user).thenReturn(user);
        when(status.getCreatedAt()).thenReturn(DateTime.now().toDate());

        FeedItem feedItem = twitterFeedItemMapper.createFeedItemFrom(status, twitterSubscription);

        assertEquals("atwitteruser", feedItem.getAuthor());
    }

}
