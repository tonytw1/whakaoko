package uk.co.eelpieconsulting.feedlistener.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity("channels")
public class Channel {

	@Id
	ObjectId objectId;

	private String id, name, username;
	
	public Channel() {
	}
	
	public Channel(String id, String name, String username) {
		this.id = id;
		this.name = name;
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "Channel [id=" + id + ", name=" + name + ", username=" + username + "]";
	}
	
}
