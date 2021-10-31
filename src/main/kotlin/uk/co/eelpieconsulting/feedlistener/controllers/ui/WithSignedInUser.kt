package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.apache.logging.log4j.LogManager
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletRequest

abstract class WithSignedInUser(val currentUserService: CurrentUserService, val request: HttpServletRequest) {

    private val log = LogManager.getLogger(WithSignedInUser::class.java)

    fun forCurrentUser(handler: (User) -> ModelAndView): ModelAndView {
        val user = currentUserService.getCurrentUserUser()
        if (user != null) {
            log.info("Generating page for user: " + user.username)
            val mv = handler(user)
            return mv.addObject("user", user)

        } else {
            val path = request.requestURI
            log.info("No signed in user when requesting path $path; redirecting to sign in")
            // Push this path so the user can be redirected after successful auth
            request.session.setAttribute("redirect", path)
            return ModelAndView(RedirectView("/signin"))
        }
    }

}