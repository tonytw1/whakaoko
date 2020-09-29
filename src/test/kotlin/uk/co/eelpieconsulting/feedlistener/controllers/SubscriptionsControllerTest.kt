package uk.co.eelpieconsulting.feedlistener.controllers

import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscriptionManager
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.rss.RssSubscriptionManager
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterSubscriptionManager

class SubscriptionsControllerTest {

    private val usersDAO = Mockito.mock(UsersDAO::class.java)
    private val subscriptionsDAO = Mockito.mock(SubscriptionsDAO::class.java)
    private val feedItemPopulator = Mockito.mock(FeedItemPopulator::class.java)
    private val feedItemDAO = Mockito.mock(FeedItemDAO::class.java)
    private val viewFactory = Mockito.mock(ViewFactory::class.java)
    private val urlBuilder = Mockito.mock(UrlBuilder::class.java)
    private val rssSubscriptionManager = Mockito.mock(RssSubscriptionManager::class.java)
    private val rssPoller = Mockito.mock(RssPoller::class.java)
    private val twitterSubscriptionManager = Mockito.mock(TwitterSubscriptionManager::class.java)
    private val instagramSubscriptionManager = Mockito.mock(InstagramSubscriptionManager::class.java)
    private val twitterListener = Mockito.mock(TwitterListener::class.java)

    val subscriptionsController = SubscriptionsController(usersDAO,
            subscriptionsDAO,
            feedItemPopulator,
            feedItemDAO,
            viewFactory,
            urlBuilder,
            rssSubscriptionManager,
            rssPoller,
            twitterSubscriptionManager,
            instagramSubscriptionManager,
            twitterListener
    )

    @Test
    fun reloadShouldImmediatelyRepollRSSSubscription() {
        val subscription = RssSubscription("http://localhost/feed", "a-channel", "a-user");
        `when`(subscriptionsDAO.getById(subscription.id)).thenReturn(subscription)

        subscriptionsController.reload("a-user", subscription.id);

        verify(rssPoller).run(subscription);
    }

}