package uk.co.eelpieconsulting.feedlistener.controllers.ui

import com.google.common.base.Strings
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletRequest

@Controller
class UsersUIController @Autowired constructor(val channelsDAO: ChannelsDAO,
                                               val credentialService: CredentialService,
                                               val usersDAO: UsersDAO,
                                               val viewFactory: ViewFactory,
                                               currentUserService: CurrentUserService,
                                               request: HttpServletRequest) : WithSignedInUser(currentUserService, request) {

    private val log = LogManager.getLogger(UsersUIController::class.java)

    @GetMapping("/ui/newuser")
    fun newUser(): ModelAndView {
        return ModelAndView("newUser")
    }

    @PostMapping("/ui/newuser")
    fun submitNewUser(
            @RequestParam(value = "username", required = true) username: String,
            @RequestParam(value = "password", required = true) password: String
    ): ModelAndView? {
        if (Strings.isNullOrEmpty(username)) {
            throw RuntimeException("No username given")
        }
        if (usersDAO.getByUsername(username) != null) {
            throw RuntimeException("Username is not available")
        }

        val isValidPassword = password.trim().length > 6
        if (!isValidPassword) {
            throw  RuntimeException("Invalid password")
        }

        val newUser = User(username, password)
        usersDAO.save(newUser)
        log.info("Created user: " + newUser)

        return ModelAndView(viewFactory.getJsonView()).addObject("data", "ok")
    }

    @GetMapping("/")
    fun userhome(): ModelAndView? {
        fun usersHomepage(user: User): ModelAndView {
            return ModelAndView("userhome").
            addObject("channels", channelsDAO.getChannelsFor(user)).
            addObject("twitterCredentials", credentialService.hasTwitterAccessToken(user.username))
        }
        return forCurrentUser(::usersHomepage)
    }

}