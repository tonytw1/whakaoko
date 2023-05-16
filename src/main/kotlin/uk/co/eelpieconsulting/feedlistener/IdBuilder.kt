package uk.co.eelpieconsulting.feedlistener

import org.springframework.stereotype.Component
import java.util.*

@Component
class IdBuilder {
    fun makeIdFor(text: String): String {
        val lowerCasedAndTrimmed = text.lowercase(Locale.getDefault()).trim { it <= ' ' }
        return lowerCasedAndTrimmed.replace("\\s".toRegex(), "-").replace("[^\\-a-z0-9_]".toRegex(), "")
            .replace("--+".toRegex(), "-")
    }

    fun makeIdForChannel(): String {
        return UUID.randomUUID().toString()
    }

}
