package uk.co.eelpieconsulting.feedlistener.controllers

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.daos.DataStoreFactory
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO

@Controller
class UsersController  @Autowired constructor (val usersDAO: UsersDAO, val viewFactory: ViewFactory) {

    private val log = Logger.getLogger(DataStoreFactory::class.java)

    @GetMapping("/users")
    fun users(): ModelAndView? {
        return ModelAndView(viewFactory.getJsonView()).addObject("data", usersDAO.getUsers())
    }

    @PostMapping("/users")
    fun newUser(@RequestParam(value = "username", required = true) username: String?): ModelAndView? {
        usersDAO.createUser(username)
        log.info("Created user: " + username)
        return ModelAndView(viewFactory.getJsonView()).addObject("data", "ok")
    }

}