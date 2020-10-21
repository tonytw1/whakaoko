package uk.co.eelpieconsulting.feedlistener.controllers

import org.apache.struts.mock.MockHttpServletRequest
import org.apache.struts.mock.MockHttpSession
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User

class CurrentUserServiceTest {

    @Test
    fun currentUserCanBeSetAsSessionAttributeForTheUI() {
        val usersDAO = mock(UsersDAO::class.java)
        val request = MockHttpServletRequest()
        val session = MockHttpSession()
        session.setAttribute("signedInUser", "a-user");
        request.setHttpSession(session)
        val user = User("a-user")

        `when`(usersDAO.getByUsername("a-user")).thenReturn(user)

        val currentUser = CurrentUserService(request, usersDAO).getCurrentUserUser()

        assertEquals(user, currentUser)
    }

}