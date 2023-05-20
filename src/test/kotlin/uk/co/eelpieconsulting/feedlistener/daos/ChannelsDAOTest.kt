package uk.co.eelpieconsulting.feedlistener.daos

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.feedlistener.model.Channel
import java.util.*

class ChannelsDAOTest {

    private val mongoDatabase = "whakaokotest${UUID.randomUUID()}"

    private val mongoHost = run {
        var mongoHost = System.getenv("MONGO_HOST")
        if (mongoHost == null) {
            mongoHost = "localhost"
        }
        mongoHost
    }

    private val dataStoreFactory = DataStoreFactory("mongodb://$mongoHost:27017", mongoDatabase)
    private val channelsDAO = ChannelsDAO(dataStoreFactory)

    @Test
    fun canRoundTripChannels() {
        val channel = Channel(ObjectId.get(), UUID.randomUUID().toString(), "A channel", "a-user")

        channelsDAO.save(channel)

        val reloaded = channelsDAO.getById(channel.id)
        assertEquals(channel, reloaded)
    }

}
