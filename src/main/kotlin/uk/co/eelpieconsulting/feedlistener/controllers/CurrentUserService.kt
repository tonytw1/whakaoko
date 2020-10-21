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

        // API clients will have some sort of token on each request
        val headerUsername = request.getHeader(signedInUserAttribute)
        if (headerUsername != null) {
            return usersDAO.getByUsername(headerUsername as String)
        }
        return null;
    }

    fun setSignedInUser(user: User) {
        request.getSession().setAttribute(signedInUserAttribute, user.username)
    }

}