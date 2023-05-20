package uk.co.eelpieconsulting.feedlistener.model

import com.fasterxml.jackson.annotation.JsonInclude
import dev.morphia.annotations.*
import dev.morphia.utils.IndexType
import org.bson.types.ObjectId

@Entity("channels")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Indexes(
    Index(
        fields = [Field(value = "username", type = IndexType.DESC), Field(value = "name", type = IndexType.DESC)],
        options = IndexOptions(unique = true)
    )
)

data class Channel(@Id val objectId: ObjectId, val id: String, val name: String, val username: String)