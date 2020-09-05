package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User
import javax.servlet.http.HttpServletRequest

@Component
class CurrentUserService @Autowired constructor(val request: HttpServletRequest, val usersDAO: UsersDAO) {

    fun getCurrentUserUser(): User? {
        val username = request.session.getAttribute("user")
        if (username != null) {
            return usersDAO.getByUsername(username as String)
        } else {
            return null;
        }
    }

}