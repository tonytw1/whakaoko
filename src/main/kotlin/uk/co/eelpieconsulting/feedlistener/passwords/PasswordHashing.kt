package uk.co.eelpieconsulting.feedlistener.passwords

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordHashing {

    private val encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    fun hash(password: String): String {
        return encoder.encode(password)
    }

    fun matches(password: String, hashed: String): Boolean {
        return encoder.matches(password, hashed)
    }

}

