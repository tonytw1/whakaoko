package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.apache.log4j.Logger
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.model.User

abstract class WithSignedInUser(val currentUserService: CurrentUserService) {

    private val log = Logger.getLogger(WithSignedInUser::class.java)

    fun forCurrentUser(handler: (User) -> ModelAndView): ModelAndView? {
        val user = currentUserService.getCurrentUserUser()
        if (user != null) {
            log.info("Generating page for user: " + user)
            return handler(user).addObject("user", user)

        } else {
            log.info("No signed in user; redirecting to sign in.")
            return ModelAndView(RedirectView("/signin"))
        }
    }

}