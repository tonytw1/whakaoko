package uk.co.eelpieconsulting.feedlistener.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletRequest

@Component
class CurrentUserService @Autowired constructor(val request: HttpServletRequest, val usersDAO: UsersDAO) {

    private val signedInUserAttribute = "signedInUser"

    fun getCurrentUserUser(): User? {
        // UI sessions have the signed in user on the session
        val sessionUsername = request.session.getAttribute(signedInUserAttribute)
        if (sessionUsername != null) {
            return usersDAO.getByUsername(sessionUsername as String)
        }

        // API clients set a beaer token on each request
        val authorizationHeader = request.getHeader("Authorization")
        if (authorizationHeader != null) {
            val bearerPrefix = "Bearer "
            if (authorizationHeader.length < bearerPrefix.length)  {
                return null
            }
            val presentedToken = authorizationHeader.substring(bearerPrefix.length, authorizationHeader.length)
            return usersDAO.getByAccessToken(presentedToken)
        }
        return null;
    }

    fun setSignedInUser(user: User) {
        request.getSession().setAttribute(signedInUserAttribute, user.username)
    }

}