package uk.co.eelpieconsulting.feedlistener.model;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("users")
public class User {
	
	@Id
	ObjectId objectId;
	
	private String username;
	
	private String instagramAccessToken;
	private String twitterAccessToken;
	private String twitterAccessSecret;
	
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
	
	@JsonIgnore
	public String getInstagramAccessToken() {
		return instagramAccessToken;
	}
	public void setInstagramAccessToken(String instagramAccessToken) {
		this.instagramAccessToken = instagramAccessToken;
	}

	@JsonIgnore
	public String getTwitterAccessToken() {
		return twitterAccessToken;
	}
	public void setTwitterAccessToken(String twitterAccessToken) {
		this.twitterAccessToken = twitterAccessToken;
	}

	@JsonIgnore
	public String getTwitterAccessSecret() {
		return twitterAccessSecret;
	}
	public void setTwitterAccessSecret(String twitterAccessSecret) {
		this.twitterAccessSecret = twitterAccessSecret;
	}

	@Override
	public String toString() {
		return "User [instagramAccessToken=" + instagramAccessToken
				+ ", twitterAccessSecret=" + twitterAccessSecret
				+ ", twitterAccessToken=" + twitterAccessToken + ", username="
				+ username + "]";
	}
	
}
