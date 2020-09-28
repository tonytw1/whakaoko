package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import org.bson.types.ObjectId

@Entity("channels")
class Channel {
    @Id
    var objectId: ObjectId? = null
    var id: String? = null
    var name: String? = null
    var username: String? = null

    constructor() {}
    constructor(id: String?, name: String?, username: String?) {
        this.id = id
        this.name = name
        this.username = username
    }

    override fun toString(): String {
        return "Channel [id=$id, name=$name, username=$username]"
    }
}