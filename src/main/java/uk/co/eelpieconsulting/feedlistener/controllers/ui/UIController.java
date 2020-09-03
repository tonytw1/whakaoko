package uk.co.eelpieconsulting.feedlistener.controllers.ui;

import com.mongodb.MongoException;
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
import uk.co.eelpieconsulting.feedlistener.model.User;

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

    @RequestMapping(value = "/ui/newuser", method = RequestMethod.GET)
    public ModelAndView newUser() {
        return new ModelAndView("newUser");
    }

    @RequestMapping(value = "/ui/{username}", method = RequestMethod.GET)
    public ModelAndView userhome(@PathVariable String username) throws MongoException, UnknownUserException {
        usersDAO.getByUsername(username);

        final ModelAndView mv = new ModelAndView("userhome");
        mv.addObject("channels", channelsDAO.getChannels(username));
        mv.addObject("instagramCredentials", credentialService.hasInstagramAccessToken(username));
        mv.addObject("twitterCredentials", credentialService.hasTwitterAccessToken(username));
        return mv;
    }

}
