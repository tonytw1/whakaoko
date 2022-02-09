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
    private String password;
    private String googleUserId;

    private String twitterAccessToken;
    private String twitterAccessSecret;

    private String accessToken;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @JsonIgnore
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getGoogleUserId() {
        return googleUserId;
    }

    public void setGoogleUserId(String googleUserId) {
        this.googleUserId = googleUserId;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    @Override
    public String toString() {
        return "User{" +
                "objectId=" + objectId +
                ", username='" + username + '\'' +
                ", googleUserId='" + googleUserId + '\'' +
                ", twitterAccessToken='" + twitterAccessToken + '\'' +
                ", twitterAccessSecret='" + twitterAccessSecret + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }

}
