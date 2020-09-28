package uk.co.eelpieconsulting.feedlistener.model;

import java.util.Date;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity("subscriptions")
@JsonPropertyOrder({"id", "name", "channel", "url"})
public abstract class Subscription {
	
    @Id
    ObjectId objectId;
    
    private String id, name, username;  
    private Date lastRead, latestItemDate;
    private String error, etag;
    private Integer httpStatus;
  
    @Indexed
    private String channelId;
        
	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastRead() {
		return lastRead;
	}

	public void setLastRead(Date lastRead) {
		this.lastRead = lastRead;
	}

	public Date getLatestItemDate() {
		return latestItemDate;
	}

	public void setLatestItemDate(Date latestItemDate) {
		this.latestItemDate = latestItemDate;
	}
	
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	@Override
	public String toString() {
		return "Subscription{" +
				"objectId=" + objectId +
				", id='" + id + '\'' +
				", name='" + name + '\'' +
				", username='" + username + '\'' +
				", lastRead=" + lastRead +
				", latestItemDate=" + latestItemDate +
				", error='" + error + '\'' +
				", etag='" + etag + '\'' +
				", httpStatus=" + httpStatus +
				", channelId='" + channelId + '\'' +
				'}';
	}
}
