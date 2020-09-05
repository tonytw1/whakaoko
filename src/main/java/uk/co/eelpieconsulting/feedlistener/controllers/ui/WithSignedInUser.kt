package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.apache.log4j.Logger
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.model.User

abstract class WithSignedInUser(val currentUserService: CurrentUserService) {

    private val log = Logger.getLogger(WithSignedInUser::class.java)

    fun forCurrentUser(handler: (User) -> ModelAndView): ModelAndView? {
        val user = currentUserService.getCurrentUserUser();
        if (user != null) {
            log.info("Generating page for user: " + user)
            return handler(user)
        } else {
            // TODO redirect to signin
            return null
        }
    }

}