package uk.co.eelpieconsulting.feedlistener.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Kafka(@Value("\${kafka.bootStrapServers}") private val bootStrapServers: String, @Value("\${kafka.topic}") private val topic: String) {

    private val producer = buildProducer()

    fun publish(message: String) {
        producer?.send(ProducerRecord(topic, null, message.encodeToByteArray())) // Or asyncSend
    }

    private fun buildProducer(): KafkaProducer<String, ByteArray>? {
        return if (bootStrapServers.isNotEmpty() && topic.isNotEmpty()) { KafkaProducer(
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
