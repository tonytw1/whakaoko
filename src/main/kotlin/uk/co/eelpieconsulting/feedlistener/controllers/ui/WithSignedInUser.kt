package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletResponse

abstract class WithSignedInUser(val currentUserService: CurrentUserService, val response: HttpServletResponse) {

    private val log = LogManager.getLogger(WithSignedInUser::class.java)

    fun forCurrentUser(handler: (User) -> ModelAndView?): ModelAndView? {
        val user = currentUserService.getCurrentUserUser()
        if (user != null) {
            log.info("Generating page for user: " + user)
            val mv = handler(user)
            if (mv != null) {
                return mv.addObject("user", user)
            } else {
                response.sendError(HttpStatus.NOT_FOUND.value())
                return null
            }

        } else {
            log.info("No signed in user; redirecting to sign in.")
            return ModelAndView(RedirectView("/signin"))
        }
    }

}