package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.model.User

abstract class WithSignedInUser(val currentUserService: CurrentUserService) {

    fun forCurrentUser(handler: (User) -> ModelAndView): ModelAndView? {
        val user = currentUserService.getCurrentUserUser();
        if (user != null) {
            return handler(user)
        } else {
            // TODO redirect to signin
            return null
        }
    }

}