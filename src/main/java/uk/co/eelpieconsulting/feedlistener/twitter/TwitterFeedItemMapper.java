package uk.co.eelpieconsulting.feedlistener.twitter;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Status;
import uk.co.eelpieconsulting.feedlistener.model.FeedItem;
import uk.co.eelpieconsulting.feedlistener.model.Subscription;

@Component
public class TwitterFeedItemMapper {
    public FeedItem createFeedItemFrom(Status status, Subscription subscription) {
        final uk.co.eelpieconsulting.feedlistener.model.Place place = extractLocationFrom(status);
        final String mediaUrl = extractImageUrl(status);
        final String author = extractAuthorFrom(status);
        return new FeedItem(extractHeadingFrom(status), extractUrlFrom(status), null, status.getCreatedAt(), place, mediaUrl,
                author, subscription.getId(), subscription.getChannelId(), null);
    }

    private String extractAuthorFrom(Status status) {
        if (status.getUser() == null) {
            return null;
        }
        return status.getUser().getScreenName();
    }

    private String extractHeadingFrom(Status status) {
        return status.getText();
    }

    private String extractUrlFrom(Status status) {
        return "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + Long.toString(status.getId());
    }

    private uk.co.eelpieconsulting.feedlistener.model.Place extractLocationFrom(Status status) {
        uk.co.eelpieconsulting.feedlistener.model.Place place = null;
        if (status.getGeoLocation() != null) {
            final GeoLocation geoLocation = status.getGeoLocation();
            uk.co.eelpieconsulting.feedlistener.model.LatLong latLong = new uk.co.eelpieconsulting.feedlistener.model.LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
            place = new uk.co.eelpieconsulting.feedlistener.model.Place(null, latLong);    // TODO capture twitter location ids
        }
        return place;
    }

    private String extractImageUrl(Status status) {
        if (status.getMediaEntities().length > 0) {
            final MediaEntity mediaEntity = status.getMediaEntities()[0];
            String type = mediaEntity.getType();
            String url = mediaEntity.getMediaURL();
            if (!Strings.isNullOrEmpty(type) && type.equals("photo") && !Strings.isNullOrEmpty(url)) {
                return url;
            }
        }
        return null;
    }

}
