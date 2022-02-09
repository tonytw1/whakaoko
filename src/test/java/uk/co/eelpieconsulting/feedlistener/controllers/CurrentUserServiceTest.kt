package uk.co.eelpieconsulting.feedlistener.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import uk.co.eelpieconsulting.feedlistener.model.User

class CurrentUserServiceTest {

    private val usersDAO = mock(UsersDAO::class.java)

    private val user = User("a-user", "with-a-password")

    @Test
    fun currentUserCanBeSetAsSessionAttributeForTheUI() {
        val request = MockHttpServletRequest()
        request.session.setAttribute("signedInUser", "an-oid");
        `when`(usersDAO.getByObjectId("an-oid")).thenReturn(user)

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

