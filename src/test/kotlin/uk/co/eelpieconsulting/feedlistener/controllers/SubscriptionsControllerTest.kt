package uk.co.eelpieconsulting.feedlistener.controllers

import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletRequest

import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.RssSubscription
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.rss.RssPoller
import uk.co.eelpieconsulting.feedlistener.views.ViewFactory

class SubscriptionsControllerTest {

    private val channelsDAO = mock(ChannelsDAO::class.java)
    private val subscriptionsDAO = mock(SubscriptionsDAO::class.java)
    private val feedItemPopulator = mock(FeedItemPopulator::class.java)
    private val feedItemDAO = mock(FeedItemDAO::class.java)
    private val viewFactory = mock(ViewFactory::class.java)
    private val urlBuilder = mock(UrlBuilder::class.java)
    private val rssPoller = mock(RssPoller::class.java)
    private val currentUserService = mock(CurrentUserService::class.java)

    private val subscriptionsController = SubscriptionsController(subscriptionsDAO,
            channelsDAO,
            feedItemPopulator,
            feedItemDAO,
            viewFactory,
            urlBuilder,
            rssPoller,
            ConditionalLoads(channelsDAO, subscriptionsDAO),
            currentUserService,
            MockHttpServletRequest()
    )

    @Test
    fun readShouldImmediatelyRequestRepollOfRSSSubscription() {
        val auser = User(ObjectId.get(), "a-user")
        `when`(currentUserService.getCurrentUserUser()).thenReturn(auser)
        val subscription = RssSubscription("http://localhost/feed", "a-channel", "a-user")
        `when`(subscriptionsDAO.getById(subscription.id)).thenReturn(subscription)

        subscriptionsController.reload(subscription.id)

        verify(rssPoller).requestRead(subscription)
    }

    @Test
    fun deletingASubscriptionShouldRemoveThatSubscriptionsFeedItems() {
        val aUser = User(ObjectId.get(), "a-user")
        `when`(currentUserService.getCurrentUserUser()).thenReturn(aUser)
        val subscription = RssSubscription("http://localhost/feed", "a-channel", aUser.username)
        `when`(subscriptionsDAO.getById(subscription.id)).thenReturn(subscription)

        subscriptionsController.deleteSubscription(subscription.id)

        verify(feedItemDAO).deleteSubscriptionFeedItems(subscription)
    }

}