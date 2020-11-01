package uk.co.eelpieconsulting.feedlistener;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdBuilderTest {

    @Test
    public void canGenerateHumanReadableIdFromText() {
        assertEquals("the-quick-brown-fox", new IdBuilder().makeIdFor("The quick, brown fox."));
    }

}
