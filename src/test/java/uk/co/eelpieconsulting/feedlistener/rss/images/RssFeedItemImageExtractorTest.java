package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.synd.SyndEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RssFeedItemImageExtractorTest {

    private static final String HTML_CONTAINING_IMAGE_TAG = "html containing image tag";
    private static final String FULLY_QUALIFIED_IMAGE_URL = "http://www.localhost/images/test.jpg";
    private static final String IMAGE_PATH = "/images/test.jpg";

    private BodyHtmlImageExtractor bodyHtmlImageExtractor = mock(BodyHtmlImageExtractor.class);


    private SyndEntry itemWithImageInHtmlBody = mock(SyndEntry.class);

    private RssFeedItemImageExtractor extractor = new RssFeedItemImageExtractor(bodyHtmlImageExtractor, new MediaModuleImageExtractor());

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
