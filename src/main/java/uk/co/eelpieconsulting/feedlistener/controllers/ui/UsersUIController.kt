package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.feedlistener.credentials.CredentialService
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO

@Controller
class UsersUIController @Autowired constructor(val usersDAO: UsersDAO, val channelsDAO: ChannelsDAO, val credentialService: CredentialService) {

    @GetMapping("/ui/newuser")
    fun newUser(): ModelAndView? {
        return ModelAndView("newUser")
    }

    @GetMapping("/ui/{username}")
    fun userhome(@PathVariable username: String?): ModelAndView? {
        usersDAO.getByUsername(username)
        return ModelAndView("userhome").
        addObject("channels", channelsDAO.getChannels(username)).
        addObject("instagramCredentials", credentialService.hasInstagramAccessToken(username)).
        addObject("twitterCredentials", credentialService.hasTwitterAccessToken(username))
    }

}