package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User
import java.util.*
import javax.servlet.http.HttpServletResponse

@Controller
class AccessTokenController @Autowired constructor(val urlBuilder: UrlBuilder,
                                                   val usersDAO: UsersDAO,
                                                   currentUserService: CurrentUserService,
                                                   response: HttpServletResponse) : WithSignedInUser(currentUserService, response) {

    @GetMapping("/generate-access-token")
    fun generate(): ModelAndView? {
        fun generateAccessToken(user: User): ModelAndView? {
            val token = UUID.randomUUID().toString()
            user.accessToken = token
            usersDAO.save(user)
            return ModelAndView(RedirectView(urlBuilder.userUrl))
        }

        return forCurrentUser(::generateAccessToken)
    }

}
