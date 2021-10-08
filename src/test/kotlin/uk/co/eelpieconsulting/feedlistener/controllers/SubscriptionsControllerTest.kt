package uk.co.eelpieconsulting.feedlistener.controllers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletResponse
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterListener
import uk.co.eelpieconsulting.feedlistener.twitter.TwitterSubscriptionManager

class SubscriptionsControllerTest {

    private val channelsDAO = mock(ChannelsDAO::class.java)
    private val subscriptionsDAO = mock(SubscriptionsDAO::class.java)
    private val feedItemPopulator = mock(FeedItemPopulator::class.java)
    private val feedItemDAO = mock(FeedItemDAO::class.java)
    private val viewFactory = mock(ViewFactory::class.java)
    private val urlBuilder = mock(UrlBuilder::class.java)
    private val rssPoller = mock(RssPoller::class.java)
    private val twitterSubscriptionManager = mock(TwitterSubscriptionManager::class.java)
    private val twitterListener = mock(TwitterListener::class.java)
    private val currentUserService = mock(CurrentUserService::class.java)

    val subscriptionsController = SubscriptionsController(subscriptionsDAO,
            channelsDAO,
            feedItemPopulator,
            feedItemDAO,
            viewFactory,
            urlBuilder,
            rssPoller,
            twitterSubscriptionManager,
            twitterListener,
            currentUserService,
            MockHttpServletResponse()
    )

    @Test
    fun reloadShouldImmediatelyRepollRSSSubscription() {
        `when`(currentUserService.getCurrentUserUser()).thenReturn(User())
        val subscription = RssSubscription("http://localhost/feed", "a-channel", "a-user")
        `when`(subscriptionsDAO.getByRssSubscriptionById(subscription.id)).thenReturn(subscription)

        subscriptionsController.reload(subscription.id)

        verify(rssPoller).requestRead(subscription)
    }

    @Test
    fun deletingASubscriptionShouldRemoveThatSubscriptionsFeedItems() {
        `when`(currentUserService.getCurrentUserUser()).thenReturn(User())
        val subscription = RssSubscription("http://localhost/feed", "a-channel", "a-user")
        `when`(subscriptionsDAO.getById(subscription.id)).thenReturn(subscription)

        subscriptionsController.deleteSubscription(subscription.id)

        verify(feedItemDAO).deleteSubscriptionFeedItems(subscription)
        verifyNoInteractions(twitterSubscriptionManager)
    }

}