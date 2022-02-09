package uk.co.eelpieconsulting.feedlistener.passwords

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordHashingTest {

    @Test
    fun canVerifyHashedPassword() {
        val encoder = PasswordHashing()
        val password = "password123!"

        val hashed = encoder.hash(password)

        assertFalse(hashed.contains(password))
        assertTrue(encoder.matches(password, hashed))
        assertFalse(encoder.matches("uncorrect password", hashed))
    }

}