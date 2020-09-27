package uk.co.eelpieconsulting.feedlistener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexDirection;
import org.bson.types.ObjectId;
import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.views.rss.RssFeedable;

import java.io.Serializable;
import java.util.Date;

import static dev.morphia.utils.IndexType.DESC;

@Entity("feeditems")
@Indexes({
        @Index(fields = {@Field(value = "date", type = DESC), @Field(value = "_id")}),
        @Index(fields = {@Field(value = "subscriptionId"), @Field(value = "date", type = DESC), @Field(value = "_id")}),
})

public class FeedItem implements Serializable, RssFeedable {

    private static final long serialVersionUID = 1L;

    @Id
    ObjectId objectId;

    private String title;

    @Indexed
    private String url;

    private String body;

    @Indexed(value = IndexDirection.DESC)        // TODO unused - because always used with subscription id?
    private Date date;

    private uk.co.eelpieconsulting.feedlistener.model.Place place;

    private String imageUrl;

    @Indexed
    private String subscriptionId;

    @Indexed
    private String channelId;

    private String author;

    // Display only field
    private String subscriptionName;

    public FeedItem() {
    }

    public FeedItem(String title, String url, String body,
                    Date date, uk.co.eelpieconsulting.feedlistener.model.Place place,
                    String imageUrl, String author,
                    String channelId) {
        this.title = title;
        this.url = url;
        this.body = body;
        this.date = date;
        this.place = place;
        this.imageUrl = imageUrl;
        this.author = author;
        this.channelId = channelId;
    }

    public String getId() {
        return getGUID();
    }

    private String getGUID() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public Date getDate() {
        return date;
    }

    public uk.co.eelpieconsulting.feedlistener.model.Place getPlace() {
        return place;
    }

    @JsonIgnore
    public LatLong getLatLong() {
        if (place != null && place.getLatLong() != null) {
            return new uk.co.eelpieconsulting.common.geo.model.LatLong(place.getLatLong().getLatitude(), place.getLatLong().getLongitude());
        }
        return null;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isGeoTagged() {
        return place != null;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    @JsonIgnore
    public String getDescription() {
        return body;
    }

    @Override
    @JsonIgnore
    public String getHeadline() {
        return title;
    }

    @Override
    @JsonIgnore
    public String getWebUrl() {
        return url;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @JsonIgnore
    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "FeedItem{" +
                "objectId=" + objectId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", body='" + body + '\'' +
                ", date=" + date +
                ", place=" + place +
                ", imageUrl='" + imageUrl + '\'' +
                ", subscriptionId='" + subscriptionId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", author='" + author + '\'' +
                ", subscriptionName='" + subscriptionName + '\'' +
                '}';
    }
}
