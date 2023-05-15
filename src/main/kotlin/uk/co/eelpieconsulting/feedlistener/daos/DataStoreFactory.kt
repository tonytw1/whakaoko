package uk.co.eelpieconsulting.feedlistener.daos

import com.mongodb.MongoException
import com.mongodb.client.MongoClients
import dev.morphia.Datastore
import dev.morphia.Morphia
import dev.morphia.mapping.DiscriminatorFunction
import dev.morphia.mapping.MapperOptions
import org.apache.logging.log4j.LogManager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.model.*

@Component
class DataStoreFactory @Autowired constructor(
    @Value("\${mongo.uri}") private val mongoUri: String,
    @Value("\${mongo.database}") private val mongoDatabase: String
) {

    private val log = LogManager.getLogger(DataStoreFactory::class.java)

    private val datastore: Datastore

    init {
        datastore = createDataStore(mongoUri, mongoDatabase)
        datastore.ensureIndexes()
    }

    fun get(): Datastore {
        return datastore
    }

    private fun createDataStore(mongoUri: String, database: String): Datastore {
        return try {
            val mapperOptions =
                MapperOptions.builder().discriminatorKey("className").discriminator(DiscriminatorFunction.className())
                    .enablePolymorphicQueries(true).build()
            val mongoClient = MongoClients.create(mongoUri)
            val datastore = Morphia.createDatastore(mongoClient, database, mapperOptions)

            // These explicit mappings are needed to trigger subclass querying during finds;
            // see subtype in LegacyQuery source code for hints
            datastore.mapper.map(TwitterTagSubscription::class.java)
            datastore.mapper.map(RssSubscription::class.java)
            datastore

        } catch (e: MongoException) {
            log.error(e)
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }
}