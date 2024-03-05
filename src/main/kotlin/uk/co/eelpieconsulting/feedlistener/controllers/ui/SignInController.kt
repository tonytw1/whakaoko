package uk.co.eelpieconsulting.feedlistener.controllers.ui

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.passwords.PasswordHashing

@Controller
class SignInController @Autowired constructor(
    private val usersDAO: UsersDAO,
    private val currentUserService: CurrentUserService,
    private val passwordHashing: PasswordHashing
) {

    private val log = LogManager.getLogger(SignInController::class.java)

    @GetMapping("/signin")
    fun signinPrompt(session: HttpSession, redirectAttributes: RedirectAttributes): ModelAndView {
        return ModelAndView("signin")
    }

    @PostMapping("/signin")
    fun signin(
        username: String,
        password: String,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): ModelAndView {
        log.info("Signing in as: $username")
        val user = usersDAO.getByUsername(username)
        if (user != null) {
            val userPersistedHashedPassword = user.password
            if (userPersistedHashedPassword != null) {
                if (userPersistedHashedPassword.isNotEmpty() && passwordHashing.matches(
                        password,
                        userPersistedHashedPassword
                    )
                ) {
                    currentUserService.setSignedInUser(user)
                    return redirectToSignedInUserUI(request)
                } else {
                    log.info("Password incorrect")
                }
            }
        }
        return redirectToSigninPromptWithError(
            "We could not find a user with this username and password",
            redirectAttributes
        )
    }

    private fun redirectToSigninPromptWithError(error: String, redirectAttributes: RedirectAttributes): ModelAndView {
        redirectAttributes.addFlashAttribute("error", error);
        return redirectToSigninPrompt()
    }

    private fun redirectToSigninPrompt(): ModelAndView {
        return ModelAndView(RedirectView("/signin"))
    }

    private fun redirectToSignedInUserUI(request: HttpServletRequest): ModelAndView {
        return ModelAndView(RedirectView("/"))
    }

}
