package uk.co.eelpieconsulting.feedlistener.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity("users")
public class User {

    @Id
    ObjectId objectId;

    private String username;

    private String instagramAccessToken;
    private String twitterAccessToken;
    private String twitterAccessSecret;

    private String accessToken;

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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "objectId=" + objectId +
                ", username='" + username + '\'' +
                '}';
    }

}
