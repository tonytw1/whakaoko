package uk.co.eelpieconsulting.feedlistener.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("users")
public class User {
	
	@Id
	ObjectId objectId;
	private String username;
	
	public User() {
	}
	
	public User(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String toString() {
		return "User [username=" + username + "]";
	}
	
}
