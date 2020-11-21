package uk.co.eelpieconsulting.feedlistener.controllers

import com.google.common.base.Strings
import org.apache.log4j.LogManager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.daos.DataStoreFactory
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User

@Controller
class UsersController @Autowired constructor(val usersDAO: UsersDAO, val viewFactory: ViewFactory) {

    private val log = LogManager.getLogger(DataStoreFactory::class.java)

    @GetMapping("/users")
    fun users(): ModelAndView? {
        return ModelAndView(viewFactory.getJsonView()).addObject("data", usersDAO.getUsers())
    }

    @PostMapping("/users")
    fun newUser(@RequestParam(value = "username", required = true) username: String): ModelAndView? {
        if (Strings.isNullOrEmpty(username)) {
            throw RuntimeException("No username given")
        }
        if (usersDAO.getByUsername(username) != null) {
            throw RuntimeException("Username is not available")
        }

        val newUser = User(username)
        usersDAO.save(newUser)
        log.info("Created user: " + newUser)

        return ModelAndView(viewFactory.getJsonView()).addObject("data", "ok")
    }

}