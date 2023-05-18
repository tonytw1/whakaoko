package uk.co.eelpieconsulting.feedlistener.controllers.ui

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import java.util.*

@Controller
class AccessTokenController @Autowired constructor(val urlBuilder: UrlBuilder,
                                                   private val usersDAO: UsersDAO,
                                                   currentUserService: CurrentUserService,
                                                   request: HttpServletRequest) : WithSignedInUser(currentUserService, request) {

    @GetMapping("/generate-access-token")
    fun generate(): ModelAndView {
        return forCurrentUser { user ->
            val token = UUID.randomUUID().toString()
            user.accessToken = token
            usersDAO.save(user)
            ModelAndView(RedirectView(urlBuilder.userUrl))
        }
    }

}
