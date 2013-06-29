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
    
    private String id, name;
    
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
	
}
