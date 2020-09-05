package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.model.User

@Controller
class UsersUIController @Autowired constructor(val channelsDAO: ChannelsDAO,
                                               val credentialService: CredentialService,
                                               currentUserService: CurrentUserService) : WithSignedInUser(currentUserService) {

    @GetMapping("/ui/newuser")
    fun newUser(): ModelAndView? {
        return ModelAndView("newUser")
    }

    @GetMapping("/ui")
    fun userhome(): ModelAndView? {
        fun usersHomepage(user: User): ModelAndView {
            return ModelAndView("userhome").
            addObject("channels", channelsDAO.getChannels(user.username)).
            addObject("instagramCredentials", credentialService.hasInstagramAccessToken(user.username)).
            addObject("twitterCredentials", credentialService.hasTwitterAccessToken(user.username))
        }

        return forCurrentUser(::usersHomepage)
    }

}