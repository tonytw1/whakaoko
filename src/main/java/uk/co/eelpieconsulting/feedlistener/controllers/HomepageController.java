package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;

import com.mongodb.MongoException;

@Controller
public class HomepageController {
	
	private final ChannelsDAO channelsDAO;
	private final UsersDAO usersDAO;
	
	@Autowired
	public HomepageController(UsersDAO usersDAO, ChannelsDAO channelsDAO) {
		this.usersDAO = usersDAO;
		this.channelsDAO = channelsDAO;
	}
	
	@RequestMapping(value="/ui/{username}", method=RequestMethod.GET)
	public ModelAndView homepage(@PathVariable String username) throws UnknownHostException, MongoException {
		if (usersDAO.getByUsername(username) == null) {
			throw new RuntimeException("Invalid user");
		}
		
		final ModelAndView mv = new ModelAndView("homepage");
		mv.addObject("channels", channelsDAO.getChannels(username));		
		return mv;
	}
	
}
