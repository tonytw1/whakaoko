package uk.co.eelpieconsulting.feedlistener.controllers;

import com.google.common.base.Strings;
import com.mongodb.MongoException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.IdBuilder;
import uk.co.eelpieconsulting.feedlistener.UrlBuilder;
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;
import uk.co.eelpieconsulting.feedlistener.exceptions.UnknownUserException;
import uk.co.eelpieconsulting.feedlistener.model.Channel;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

import java.net.UnknownHostException;
import java.util.List;

@Controller
public class ChannelsController {

    private final static Logger log = Logger.getLogger(ChannelsController.class);

    private final UsersDAO usersDAO;
    private final ChannelsDAO channelsDAO;
    private final SubscriptionsDAO subscriptionsDAO;
    private final IdBuilder idBuilder;
    private final UrlBuilder urlBuilder;
    private final ViewFactory viewFactory;
    private final FeedItemPopulator feedItemPopulator;

    @Autowired
    public ChannelsController(UsersDAO usersDAO, ChannelsDAO channelsDAO, SubscriptionsDAO subscriptionsDAO,
                              IdBuilder idBuilder, UrlBuilder urlBuilder, ViewFactory viewFactory,
                              FeedItemPopulator feedItemPopulator) {
        this.usersDAO = usersDAO;
        this.channelsDAO = channelsDAO;
        this.subscriptionsDAO = subscriptionsDAO;
        this.idBuilder = idBuilder;
        this.urlBuilder = urlBuilder;
        this.viewFactory = viewFactory;
        this.feedItemPopulator = feedItemPopulator;
    }

    @RequestMapping(value = "/{username}/channels", method = RequestMethod.GET)
    public ModelAndView channelsJson(@PathVariable String username) throws UnknownUserException {
        usersDAO.getByUsername(username);

        log.info("Channels for user: " + username);
        final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
        mv.addObject("data", channelsDAO.getChannels(username));
        return mv;
    }

    @RequestMapping(value = "/{username}/channels/{id}", method = RequestMethod.GET)
    public ModelAndView channel(@PathVariable String username, @PathVariable String id) throws UnknownHostException, MongoException, UnknownUserException {
        usersDAO.getByUsername(username);

        final Channel channel = channelsDAO.getById(username, id);

        final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
        mv.addObject("data", channel);
        return mv;
    }

    @RequestMapping(value = "/{username}/channels/{id}/subscriptions", method = RequestMethod.GET)
    public ModelAndView channelSubscriptions(@PathVariable String username, @PathVariable String id, @RequestParam(required = false) String url) throws UnknownHostException, MongoException, UnknownUserException {
        usersDAO.getByUsername(username);

        final Channel channel = channelsDAO.getById(username, id);
        final List<Subscription> subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(username, channel.getId(), url);

        final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
        mv.addObject("data", subscriptionsForChannel);
        return mv;
    }

    @RequestMapping(value = "/{username}/channels/{id}/items", method = RequestMethod.GET)
    public ModelAndView channelJson(@PathVariable String username, @PathVariable String id,
                                    @RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer pageSize,
                                    @RequestParam(required = false) String format,
                                    @RequestParam(required = false) String q
    ) throws UnknownHostException, MongoException, UnknownUserException {
        usersDAO.getByUsername(username);
        final Channel channel = channelsDAO.getById(username, id);

        ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
        if (!Strings.isNullOrEmpty(format) && format.equals("rss")) {    // TODO view factory could do this?
            mv = new ModelAndView(viewFactory.getRssView(channel.getName(), urlBuilder.getChannelUrl(channel), ""));
        }

        feedItemPopulator.populateFeedItems(username, channel, page, mv, "data", pageSize, q);
        return mv;
    }

    @RequestMapping(value = "/{username}/channels", method = RequestMethod.POST)
    public ModelAndView addChannel(@PathVariable String username, @RequestParam String name) throws UnknownUserException {
        usersDAO.getByUsername(username);

        Channel newChannel = new Channel(idBuilder.makeIdFor(name), name, username);
        channelsDAO.add(username, newChannel);

        return new ModelAndView(new RedirectView(urlBuilder.getChannelUrl(newChannel)));
    }

}
