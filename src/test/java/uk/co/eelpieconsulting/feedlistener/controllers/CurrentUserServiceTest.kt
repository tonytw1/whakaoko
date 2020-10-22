package uk.co.eelpieconsulting.feedlistener.controllers

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User
class CurrentUserServiceTest {

    private val usersDAO = mock(UsersDAO::class.java)

    private val user = User("a-user")

    @Test
    fun currentUserCanBeSetAsSessionAttributeForTheUI() {
        val request = MockHttpServletRequest()
        request.session.setAttribute("signedInUser", "a-user");
        `when`(usersDAO.getByUsername("a-user")).thenReturn(user)

        val currentUser = CurrentUserService(request, usersDAO).getCurrentUserUser()

        assertEquals(user, currentUser)
    }

    @Test
    fun currentUserCanBeSpecifiedInByBearerTokenOnApiRequests() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer some-token")
        `when`(usersDAO.getByAccessToken("some-token")).thenReturn(user)

        val currentUser = CurrentUserService(request, usersDAO).getCurrentUserUser()

        assertEquals(user, currentUser)
    }

}

