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
                ", accessToken='" + accessToken + '\'' +
                '}';
    }

}
