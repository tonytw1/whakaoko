package uk.co.eelpieconsulting.feedlistener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexDirection;
import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.common.views.rss.RssFeedable;

import java.io.Serializable;
import java.util.Date;

import static org.mongodb.morphia.utils.IndexType.DESC;

@Entity("feeditems")
@Indexes({
        @Index(fields = {@Field(value = "date", type = DESC), @Field(value = "_id")}),
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

    private Place place;
    private String imageUrl;

    @Indexed
    private String subscriptionId;

    private String author;

    public FeedItem() {
    }

    public FeedItem(String title, String url, String body, Date date, Place place, String imageUrl, String author) {
        this.title = title;
        this.url = url;
        this.body = body;
        this.date = date;
        this.place = place;
        this.imageUrl = imageUrl;
        this.author = author;
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

    public Place getPlace() {
        return place;
    }

    @JsonIgnore
    public LatLong getLatLong() {
        return place != null ? place.getLatLong() : null;
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

    @Override
    public String toString() {
        return "FeedItem [body=" + body + ", date=" + date + ", imageUrl="
                + imageUrl + ", objectId=" + objectId + ", place=" + place
                + ", subscriptionId=" + subscriptionId + ", title=" + title
                + ", url=" + url + "]";
    }

}
