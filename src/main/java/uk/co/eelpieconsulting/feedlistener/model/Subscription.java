package uk.co.eelpieconsulting.feedlistener.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

@Entity("subscriptions")
@JsonPropertyOrder({"id", "name", "channel", "url"})
public abstract class Subscription {
	
    @Id
    ObjectId objectId;
    
    private String id, name;
    
    private Date lastRead, latestItemDate;
    
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

	@Override
	public String toString() {
		return "Subscription [channelId=" + channelId + ", id=" + id
				+ ", lastRead=" + lastRead + ", latestItemDate="
				+ latestItemDate + ", name=" + name + "]";
	}
	
}
