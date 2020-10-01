package uk.co.eelpieconsulting.feedlistener;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IdBuilderTest {

    @Test
    public void canGenerateHumanReadableIdFromText() {
        assertEquals("the-quick-brown-fox", new IdBuilder().makeIdFor("The quick, brown fox."));
    }

}
