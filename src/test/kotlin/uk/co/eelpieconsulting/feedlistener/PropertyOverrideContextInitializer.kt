package uk.co.eelpieconsulting.feedlistener

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import org.springframework.test.context.support.TestPropertySourceUtils
import java.util.*

@Component
class PropertyOverrideContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {

        val mongoDatabase = "whakaokotest${UUID.randomUUID()}"

        val mongoHost = run {
            var mongoHost = System.getenv("MONGO_HOST")
            if (mongoHost == null) {
                mongoHost = "localhost"
            }
            mongoHost
        }

        val mongoUri = "mongodb://$mongoHost:27017"

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext, "mongo.uri=$mongoUri", "mongo.database=$mongoDatabase"
        )
    }

}