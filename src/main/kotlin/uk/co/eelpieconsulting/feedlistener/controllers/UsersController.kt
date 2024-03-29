package uk.co.eelpieconsulting.feedlistener.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.views.ViewFactory

@Controller
class UsersController @Autowired constructor(private val usersDAO: UsersDAO, private val viewFactory: ViewFactory) {

    @GetMapping("/users")
    fun users(): ModelAndView? {
        return ModelAndView(viewFactory.jsonView()).addObject("data", usersDAO.getUsers())
    }

}