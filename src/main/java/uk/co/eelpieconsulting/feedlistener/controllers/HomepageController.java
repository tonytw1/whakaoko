package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO;

import com.mongodb.MongoException;

@Controller
public class HomepageController {
	
	private final ChannelsDAO channelsDAO;
	
	@Autowired
	public HomepageController(ChannelsDAO channelsDAO) {
		this.channelsDAO = channelsDAO;
	}
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView homepage() throws UnknownHostException, MongoException {
		final ModelAndView mv = new ModelAndView("homepage");
		mv.addObject("channels", channelsDAO.getChannels());		
		return mv;
	}
}
