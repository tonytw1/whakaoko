package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import org.bson.types.ObjectId

@Entity("channels")
class Channel {
    @Id
    var objectId: ObjectId? = null
    lateinit var id: String
    lateinit var name: String
    lateinit var username: String

    constructor() {}
    constructor(id: String, name: String, username: String) {
        this.id = id
        this.name = name
        this.username = username
    }
}