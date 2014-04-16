package uk.co.eelpieconsulting.feedlistener.controllers.ui;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator;
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService;
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import com.google.common.collect.Maps;
import com.mongodb.MongoException;

@Controller
public class UIController {
		
	private ChannelsDAO channelsDAO;
	private UsersDAO usersDAO;
	private SubscriptionsDAO subscriptionsDAO;
	private FeedItemDAO feedItemDAO;
	private CredentialService credentialService;
	private FeedItemPopulator feedItemPopulator;
	
	public UIController() {
	}
	
	@Autowired
	public UIController(UsersDAO usersDAO, ChannelsDAO channelsDAO, SubscriptionsDAO subscriptionsDAO,
			FeedItemDAO feedItemDAO, CredentialService credentialService, FeedItemPopulator feedItemPopulator) {
		this.usersDAO = usersDAO;
		this.channelsDAO = channelsDAO;
		this.subscriptionsDAO = subscriptionsDAO;
		this.feedItemDAO = feedItemDAO;
		this.credentialService = credentialService;
		this.feedItemPopulator = feedItemPopulator;
	}
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView homepage() {		
		return new ModelAndView("homepage");
	}
	
	@RequestMapping(value="/ui/newuser", method=RequestMethod.GET)
	public ModelAndView newUser() {		
		return new ModelAndView("newUser");
	}
	
	@RequestMapping(value="/ui/{username}", method=RequestMethod.GET)
	public ModelAndView userhome(@PathVariable String username) throws UnknownHostException, MongoException, UnknownUserException {
		usersDAO.getByUsername(username);
		
		final ModelAndView mv = new ModelAndView("userhome");
		mv.addObject("channels", channelsDAO.getChannels(username));
		mv.addObject("instagramCredentials", credentialService.hasInstagramAccessToken(username));
		mv.addObject("twitterCredentials", credentialService.hasTwitterAccessToken(username));
		return mv;
	}
	
	@RequestMapping(value="/ui/{username}/subscriptions/new", method=RequestMethod.GET)
	public ModelAndView newSubscriptionForm(@PathVariable String username) throws UnknownUserException {
		usersDAO.getByUsername(username);
		
		final ModelAndView mv = new ModelAndView("newSubscription");
		mv.addObject("username", username);
		mv.addObject("channels", channelsDAO.getChannels(username));
		return mv;
	}
	
	@RequestMapping(value="/ui/{username}/subscriptions/{id}", method=RequestMethod.GET)
	public ModelAndView subscription(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException, UnknownSubscriptionException, UnknownUserException {
		usersDAO.getByUsername(username);
		
		Subscription subscription = subscriptionsDAO.getById(username, id);
		if (subscription == null) {
			throw new RuntimeException("Invalid user");
		}
		
		ModelAndView mv = new ModelAndView("subscription");
		mv.addObject("subscription", subscription);
		mv.addObject("subscriptionSize", feedItemDAO.getSubscriptionFeedItemsCount(subscription.getId()));
		feedItemPopulator.populateFeedItems(subscription, page, mv, "feedItems");		
		return mv;
	}
	
	@RequestMapping(value="/ui/{username}/channels/{id}", method=RequestMethod.GET)
	public ModelAndView channel(@PathVariable String username, @PathVariable String id,
			@RequestParam(required=false) Integer page) throws UnknownHostException, MongoException {
		final Channel channel = channelsDAO.getById(username, id);

		final ModelAndView mv = new ModelAndView("channel");
		mv.addObject("channel", channel);
		
		final List<Subscription> subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(username, channel.getId());
		mv.addObject("subscriptions", subscriptionsForChannel);
		
		if (!subscriptionsForChannel.isEmpty()) {
			mv.addObject("inboxSize", feedItemDAO.getChannelFeedItemsCount(channel.getId(), username));
			
			feedItemPopulator.populateFeedItems(username, channel, page, mv, "inbox");
			
			final Map<String, Long> subscriptionCounts = Maps.newHashMap();
			for (Subscription subscription : subscriptionsForChannel) {
				subscriptionCounts.put(subscription.getId(), feedItemDAO.getSubscriptionFeedItemsCount(subscription.getId()));	// TODO slow on channels with many subscriptions - cache or index?
			}
			mv.addObject("subscriptionCounts", subscriptionCounts);
		}
		return mv;
	}
	
	@RequestMapping(value="/ui/{username}/channels/new", method=RequestMethod.GET)
	public ModelAndView newChannelForm() {
		return new ModelAndView("newChannel");		
	}
	
}
