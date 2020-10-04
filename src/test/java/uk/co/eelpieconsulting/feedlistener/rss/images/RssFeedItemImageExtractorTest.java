package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.UrlReference;
import com.sun.syndication.feed.synd.SyndEntry;
import org.junit.Test;
import uk.co.eelpieconsulting.feedlistener.rss.RssFeedItemBodyExtractor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RssFeedItemImageExtractorTest {

    private static final String HTML_CONTAINING_IMAGE_TAG = "html containing image tag";
    private static final String FULLY_QUALIFIED_IMAGE_URL = "http://www.localhost/images/test.jpg";
    private static final String IMAGE_PATH = "/images/test.jpg";

    private RssFeedItemBodyExtractor rssFeedItemBodyExtractor = mock(RssFeedItemBodyExtractor.class);
    private BodyHtmlImageExtractor htmlImageExtractor = mock(BodyHtmlImageExtractor.class);

    private SyndEntry itemWithImage = mock(SyndEntry.class);
    private SyndEntry itemWithImageInHtmlBody = mock(SyndEntry.class);
    private MediaEntryModuleImpl mediaModuleWithImage = mock(MediaEntryModuleImpl.class);

    private RssFeedItemImageExtractor extractor = new RssFeedItemImageExtractor(rssFeedItemBodyExtractor, htmlImageExtractor);

    @Test
    public void shouldExtractUrlOfFullyQualifidedMediaRssImageElements() throws Exception {
        MediaContent image = new MediaContent(new UrlReference(FULLY_QUALIFIED_IMAGE_URL));
        MediaContent[] mediaContents = {image};

        when(itemWithImage.getModule(MediaModule.URI)).thenReturn(mediaModuleWithImage);
        when(mediaModuleWithImage.getMediaContents()).thenReturn(mediaContents);

        final String imageUrl = extractor.extractImageFrom(itemWithImage);

        assertEquals(FULLY_QUALIFIED_IMAGE_URL, imageUrl);
    }

    @Test
    public void shouldExtractFirstImageFromHtmlItemBodyIfAvailable() throws Exception {
        when(itemWithImageInHtmlBody.getModule(MediaModule.URI)).thenReturn(null);
        when(rssFeedItemBodyExtractor.extractBody(itemWithImageInHtmlBody)).thenReturn(HTML_CONTAINING_IMAGE_TAG);
        when(htmlImageExtractor.extractImage(HTML_CONTAINING_IMAGE_TAG)).thenReturn(FULLY_QUALIFIED_IMAGE_URL);

        final String imageUrl = extractor.extractImageFrom(itemWithImageInHtmlBody);

        assertEquals(FULLY_QUALIFIED_IMAGE_URL, imageUrl);
    }

    @Test
    public void shouldExtendReferencedFromTheRootImagesFoundInHtmlIntoFullyQualifiedUrlsBasedOnTheItemUrl() throws Exception {
        when(itemWithImageInHtmlBody.getLink()).thenReturn("http://www.localhost/posts/123");
        when(itemWithImageInHtmlBody.getModule(MediaModule.URI)).thenReturn(null);
        when(rssFeedItemBodyExtractor.extractBody(itemWithImageInHtmlBody)).thenReturn(HTML_CONTAINING_IMAGE_TAG);
        when(htmlImageExtractor.extractImage(HTML_CONTAINING_IMAGE_TAG)).thenReturn(IMAGE_PATH);

        final String imageUrl = extractor.extractImageFrom(itemWithImageInHtmlBody);

        assertEquals(FULLY_QUALIFIED_IMAGE_URL, imageUrl);
    }

}
