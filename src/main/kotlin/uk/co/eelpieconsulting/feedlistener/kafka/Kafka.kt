package uk.co.eelpieconsulting.feedlistener.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Kafka(@Value("\${kafka.bootStrapServers}") private val bootStrapServers: String) {

    private val producer = buildProducer()

    fun publish(channelId: String, message: String) {
        if (channelId.isNotEmpty()) {
            val topic = "whakaoko.$channelId"
            producer?.send(ProducerRecord(topic, null, message.encodeToByteArray())) // Or asyncSend
        }
    }

    private fun buildProducer(): KafkaProducer<String, ByteArray>? {
        return if (bootStrapServers.isNotEmpty()) { KafkaProducer(
            mapOf(
                "bootstrap.servers" to bootStrapServers,
                "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
                "security.protocol" to "PLAINTEXT"
            )
        )
        } else {
            null
        }
    }

}
