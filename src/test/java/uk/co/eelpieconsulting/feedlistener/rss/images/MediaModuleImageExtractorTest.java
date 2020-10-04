package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.UrlReference;
import com.sun.syndication.feed.synd.SyndEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MediaModuleImageExtractorTest {

    private static final String FULLY_QUALIFIED_IMAGE_URL = "http://www.localhost/images/test.jpg";

    private SyndEntry itemWithImage = mock(SyndEntry.class);
    private MediaEntryModuleImpl mediaModuleWithImage = mock(MediaEntryModuleImpl.class);

    private MediaModuleImageExtractor extractor = new MediaModuleImageExtractor();

    @Test
    public void shouldExtractUrlOfFullyQualifiedMediaRssImageElements() throws Exception {
        MediaContent image = new MediaContent(new UrlReference(FULLY_QUALIFIED_IMAGE_URL));
        MediaContent[] mediaContents = {image};

        when(itemWithImage.getModule(MediaModule.URI)).thenReturn(mediaModuleWithImage);
        when(mediaModuleWithImage.getMediaContents()).thenReturn(mediaContents);

        final String imageUrl = extractor.extractImageFromMediaModule(itemWithImage);

        assertEquals(FULLY_QUALIFIED_IMAGE_URL, imageUrl);
    }

}
