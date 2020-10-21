package uk.co.eelpieconsulting.feedlistener.controllers

import org.junit.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletResponse
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.instagram.InstagramSubscriptionManager
import uk.co.eelpieconsulting.feedlistener.model.InstagramTagSubscription
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.rss.RssSubscriptionManager
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterSubscriptionManager

class SubscriptionsControllerTest {

    private val usersDAO = mock(UsersDAO::class.java)
    private val subscriptionsDAO = mock(SubscriptionsDAO::class.java)
    private val feedItemPopulator = mock(FeedItemPopulator::class.java)
    private val feedItemDAO = mock(FeedItemDAO::class.java)
    private val viewFactory = mock(ViewFactory::class.java)
    private val urlBuilder = mock(UrlBuilder::class.java)
    private val rssSubscriptionManager = mock(RssSubscriptionManager::class.java)
    private val rssPoller = mock(RssPoller::class.java)
    private val twitterSubscriptionManager = mock(TwitterSubscriptionManager::class.java)
    private val instagramSubscriptionManager = mock(InstagramSubscriptionManager::class.java)
    private val twitterListener = mock(TwitterListener::class.java)
    private val currentUserService = mock(CurrentUserService::class.java)

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
            twitterListener,
            currentUserService,
            MockHttpServletResponse()
    )

    @Test
    fun reloadShouldImmediatelyRepollRSSSubscription() {
        val subscription = RssSubscription("http://localhost/feed", "a-channel", "a-user")
        `when`(subscriptionsDAO.getByRssSubscriptionById(subscription.id)).thenReturn(subscription)

        subscriptionsController.reload(subscription.id)

        verify(rssPoller).run(subscription)
    }

    @Test
    fun deletingASubscriptionShouldRemoveThatSubscriptionsFeedItems() {
        val subscription = RssSubscription("http://localhost/feed", "a-channel", "a-user")
        `when`(subscriptionsDAO.getById(subscription.id)).thenReturn(subscription)

        subscriptionsController.deleteSubscription("a-user", subscription.id)

        verify(feedItemDAO).deleteSubscriptionFeedItems(subscription)
        verifyNoInteractions(instagramSubscriptionManager)
        verifyNoInteractions(twitterSubscriptionManager)
    }

    @Test
    fun deletingAnInstagramSubscriptionShouldUnsubscribeFromInstagram() {
        val instagramTagSubscription = InstagramTagSubscription("something", 123L, "", "")

        `when`(subscriptionsDAO.getById(instagramTagSubscription.id)).thenReturn(instagramTagSubscription)

        subscriptionsController.deleteSubscription("a-user", instagramTagSubscription.id)

        verify(instagramSubscriptionManager).requestUnsubscribeFrom(instagramTagSubscription.subscriptionId)
    }

}