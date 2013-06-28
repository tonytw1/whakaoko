package uk.co.eelpieconsulting.feedlistener.model;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("subscriptions")
@JsonPropertyOrder({"id", "name", "url"})
public abstract class Subscription {
	
    @Id
    ObjectId objectId;
    
    private String name;
    
	public abstract String getId();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
