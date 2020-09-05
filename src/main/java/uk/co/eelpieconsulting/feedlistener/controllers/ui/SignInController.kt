package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import javax.servlet.http.HttpServletRequest

@Controller
class SignInController @Autowired constructor(val request: HttpServletRequest, val usersDAO: UsersDAO,
                                              val currentUserService: CurrentUserService) {

    private val log = Logger.getLogger(SignInController::class.java)

    @GetMapping("/signin")
    fun signinPrompt(): ModelAndView {
        return ModelAndView("signin");
    }

    @PostMapping("/signin")
    fun signin(username: String): ModelAndView {
        log.info("Signing in as: " + username)
        val user = usersDAO.getByUsername(username)
        if (user != null) {
            currentUserService.setSignedInUser(user)
            return ModelAndView(RedirectView("/ui"))

        } else {
            log.info("Unknown user: " + username);
            return ModelAndView(RedirectView("/signin"))
        }
    }

}
