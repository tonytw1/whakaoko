package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonInclude
import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import org.bson.types.ObjectId

@Entity("channels")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Channel(@Id val objectId: ObjectId, val id: String, val name: String, val username: String)