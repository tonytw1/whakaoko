package uk.co.eelpieconsulting.feedlistener.twitter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;
import uk.co.eelpieconsulting.feedlistener.model.TwitterTagSubscription;
import uk.co.eelpieconsulting.feedlistener.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TwitterListener {

    private final static Logger log = Logger.getLogger(TwitterListener.class);

    private final SubscriptionsDAO subscriptionsDAO;
    private final CredentialService credentialService;
    private final TwitterApiFactory twitterApiFactory;
    private final FeedItemDAO feedItemDestination;
    private final TwitterFeedItemMapper twitterFeedItemMapper;
    private final UsersDAO usersDAO;

    private Map<String, TwitterStream> twitterStreams;

    @Autowired
    public TwitterListener(SubscriptionsDAO subscriptionsDAO, CredentialService credentialService, TwitterApiFactory twitterApiFactory,
                           FeedItemDAO feedItemDestination, TwitterFeedItemMapper twitterFeedItemMapper, UsersDAO usersDAO) {
        this.subscriptionsDAO = subscriptionsDAO;
        this.credentialService = credentialService;
        this.twitterApiFactory = twitterApiFactory;
        this.feedItemDestination = feedItemDestination;
        this.twitterFeedItemMapper = twitterFeedItemMapper;
        this.usersDAO = usersDAO;
        twitterStreams = Maps.newConcurrentMap();

        connect();
    }

    public synchronized void connect() {
        for (String username : usersWithTwitterSubscriptions()) {
            log.info("Connecting Twitter listener for user: " + username);
            if (credentialService.hasTwitterAccessToken(username)) {

                TwitterStream twitterStream = getTwitterStreamForUser(username);
                if (twitterStream != null) {
                    twitterStream.cleanUp();
                }

                twitterStream = setUpTwitterStreamForUser(username);
                twitterStreams.put(username, twitterStream);

            } else {
                log.warn("No twitter credentials available for user '" + username + "'; not connecting");
            }
        }
    }

    private TwitterStream setUpTwitterStreamForUser(String username) {
        final String twitterAccessTokenForUser = credentialService.getTwitterAccessTokenForUser(username);
        final String twitterAccessSecretForUser = credentialService.getTwitterAccessSecretForUser(username);
        log.info("Using twitter access credentials to create twitter stream for user " + username + ": " + twitterAccessTokenForUser + ", " + twitterAccessSecretForUser);

        TwitterStatusListener twitterListener = new TwitterStatusListener(feedItemDestination, twitterFeedItemMapper, subscriptionsDAO, username);

        TwitterStream twitterStream = twitterApiFactory.getStreamingApi(twitterAccessTokenForUser, twitterAccessSecretForUser);
        twitterStream.addListener(twitterListener);

        filterTwitterStreamByTags(twitterStream, subscriptionsDAO.getTwitterSubscriptions());
        return twitterStream;
    }

    private void filterTwitterStreamByTags(TwitterStream twitterStream, List<Subscription> twitterSubscriptions) {
        final Set<String> tagsList = Sets.newHashSet();
        for (Subscription subscription : twitterSubscriptions) {
            tagsList.add(((TwitterTagSubscription) subscription).getTag());
        }

        if (!tagsList.isEmpty()) {
            final String[] tags = tagsList.toArray(new String[tagsList.size()]);
            twitterStream.filter(new FilterQuery().track(tags));
        }
    }

    private List<String> usersWithTwitterSubscriptions() {
        final List<String> usersWithTwitterSubscriptions = Lists.newArrayList();
        List<User> users = usersDAO.getUsers();
        for (User user : users) {
            if (!subscriptionsDAO.getTwitterSubscriptionsFor(user.getUsername()).isEmpty()) {
                usersWithTwitterSubscriptions.add(user.getUsername());
            }
        }
        return usersWithTwitterSubscriptions;
    }

    private TwitterStream getTwitterStreamForUser(String username) {
        return twitterStreams.get(username);
    }

}
