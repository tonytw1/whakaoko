package uk.co.eelpieconsulting.feedlistener.rss.images;

import com.rometools.modules.mediarss.MediaEntryModuleImpl;
import com.rometools.modules.mediarss.MediaModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.UrlReference;
import com.rometools.rome.feed.synd.SyndEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
