package uk.co.eelpieconsulting.feedlistener.controllers;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO;

import com.mongodb.MongoException;

@Controller
public class UsersController {
		
	private UsersDAO usersDAO;
	private final ViewFactory viewFactory;
	
	@Autowired	
	public UsersController(UsersDAO usersDAO, ViewFactory viewFactory) {
		this.usersDAO = usersDAO;
		this.viewFactory = viewFactory;
	}
	
	@RequestMapping(value="/users", method=RequestMethod.GET)
	public ModelAndView users() throws UnknownHostException, MongoException {		
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", usersDAO.getUsers());
		return mv;
	}
	
	@RequestMapping(value="/users", method=RequestMethod.POST)
	public ModelAndView newUser(@RequestParam(value="username", required=true) String username) throws UnknownHostException, MongoException {		
		usersDAO.createUser(username);
		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
		mv.addObject("data", "ok");
		return mv;
	}


}
