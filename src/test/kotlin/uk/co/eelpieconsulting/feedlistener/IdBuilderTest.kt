package uk.co.eelpieconsulting.feedlistener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class IdBuilderTest {
    @Test
    fun canGenerateHumanReadableIdFromText() {
        assertEquals("the-quick-brown-fox", IdBuilder().makeIdFor("The quick, brown fox."))
    }
}
