package uk.co.eelpieconsulting.feedlistener.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("subscriptions")
public abstract class Subscription {
	
    @Id
    ObjectId objectId;
    
	public abstract String getId();

}
