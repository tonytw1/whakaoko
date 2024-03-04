package uk.co.eelpieconsulting.feedlistener.controllers.ui

import com.google.common.base.Strings
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.LogManager
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User
import uk.co.eelpieconsulting.feedlistener.passwords.PasswordHashing

@Controller
class UsersUIController @Autowired constructor(
    private val channelsDAO: ChannelsDAO,
    private val usersDAO: UsersDAO,
    private val viewFactory: ViewFactory,
    private val passwordHashing: PasswordHashing,
    currentUserService: CurrentUserService,
    request: HttpServletRequest
) : WithSignedInUser(currentUserService, request) {

    private val log = LogManager.getLogger(UsersUIController::class.java)

    @GetMapping("/ui/newuser")
    fun newUser(): ModelAndView {
        return ModelAndView("newUser")
    }

    @PostMapping("/ui/newuser")
    fun submitNewUser(
        @RequestParam(value = "username", required = true) username: String,
        @RequestParam(value = "password", required = true) password: String,
        redirectAttributes: RedirectAttributes
    ): ModelAndView? {
        if (Strings.isNullOrEmpty(username)) {
            return redirectToCreateUserPromptWithError("No username given", redirectAttributes)
        }
        if (usersDAO.getByUsername(username) != null) {
            return redirectToCreateUserPromptWithError("Username is not available", redirectAttributes)
        }
        val isValidPassword = password.trim().length > 6
        if (!isValidPassword) {
            return redirectToCreateUserPromptWithError("Invalid password", redirectAttributes)
        }

        val newUser = User(ObjectId.get(), username, passwordHashing.hash(password))
        usersDAO.save(newUser)
        log.info("Created user: $newUser")

        return ModelAndView(viewFactory.jsonView).addObject("data", "ok")
    }

    private fun redirectToCreateUserPromptWithError(
        error: String,
        redirectAttributes: RedirectAttributes
    ): ModelAndView {
        redirectAttributes.addFlashAttribute("error", error);
        return ModelAndView(RedirectView("/ui/newuser"))
    }

    @GetMapping("/")
    fun userhome(): ModelAndView {
        fun usersHomepage(user: User): ModelAndView {
            return ModelAndView("userhome").addObject("channels", channelsDAO.getChannelsFor(user))
        }
        return forCurrentUser(::usersHomepage)
    }

}