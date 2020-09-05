package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.http.HttpServletRequest

class CurrentUserService @Autowired constructor(val request: HttpServletRequest){

    fun getCurrentUser(): String? {
        return request.getRequestURI().split("/").toTypedArray().get(2)
    }

}