package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.rometools.modules.mediarss.MediaModule;
import com.rometools.rome.feed.synd.SyndEntry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RssFeedItemImageExtractorTest {

    private static final String HTML_CONTAINING_IMAGE_TAG = "html containing image tag";
    private static final String FULLY_QUALIFIED_IMAGE_URL = "http://www.localhost/images/test.jpg";
    private static final String IMAGE_PATH = "/images/test.jpg";

    private BodyHtmlImageExtractor bodyHtmlImageExtractor = mock(BodyHtmlImageExtractor.class);


    private SyndEntry itemWithImageInHtmlBody = mock(SyndEntry.class);

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private RssFeedItemImageExtractor extractor = new RssFeedItemImageExtractor(bodyHtmlImageExtractor,
            new MediaModuleImageExtractor(),
            meterRegistry);

    @Test
    public void shouldExtractFirstImageFromHtmlItemBodyIfAvailable() throws Exception {
        when(itemWithImageInHtmlBody.getModule(MediaModule.URI)).thenReturn(null);
        when(bodyHtmlImageExtractor.extractImageFrom(itemWithImageInHtmlBody)).thenReturn(FULLY_QUALIFIED_IMAGE_URL);

        final String imageUrl = extractor.extractImageFrom(itemWithImageInHtmlBody);

        assertEquals(FULLY_QUALIFIED_IMAGE_URL, imageUrl);
    }

    @Test
    public void shouldExtendReferencedFromTheRootImagesFoundInHtmlIntoFullyQualifiedUrlsBasedOnTheItemUrl() throws Exception {
        when(itemWithImageInHtmlBody.getLink()).thenReturn("http://www.localhost/posts/123");
        when(itemWithImageInHtmlBody.getModule(MediaModule.URI)).thenReturn(null);
        when(bodyHtmlImageExtractor.extractImageFrom(itemWithImageInHtmlBody)).thenReturn(IMAGE_PATH);

        final String imageUrl = extractor.extractImageFrom(itemWithImageInHtmlBody);

        assertEquals(FULLY_QUALIFIED_IMAGE_URL, imageUrl);
    }

}
