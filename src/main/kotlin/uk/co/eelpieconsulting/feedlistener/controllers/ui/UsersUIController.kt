package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class UsersUIController @Autowired constructor(val channelsDAO: ChannelsDAO,
                                               val credentialService: CredentialService,
                                               currentUserService: CurrentUserService,
                                               response: HttpServletResponse,
                                               request: HttpServletRequest) : WithSignedInUser(currentUserService, response, request) {

    @GetMapping("/ui/newuser")
    fun newUser(): ModelAndView {
        return ModelAndView("newUser")
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