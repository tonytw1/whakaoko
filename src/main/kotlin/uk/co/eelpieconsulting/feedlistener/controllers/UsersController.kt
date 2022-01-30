package uk.co.eelpieconsulting.feedlistener.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO

@Controller
class UsersController @Autowired constructor(val usersDAO: UsersDAO, val viewFactory: ViewFactory) {

    @GetMapping("/users")
    fun users(): ModelAndView? {
        return ModelAndView(viewFactory.getJsonView()).addObject("data", usersDAO.getUsers())
    }

}