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
        val username = request.session.getAttribute(signedInUserAttribute)
        println(username)
        if (username != null) {
            return usersDAO.getByUsername(username as String)
        } else {
            return null;
        }
    }

    fun setSignedInUser(user: User) {
        request.getSession().setAttribute(signedInUserAttribute, user.username)
    }

}