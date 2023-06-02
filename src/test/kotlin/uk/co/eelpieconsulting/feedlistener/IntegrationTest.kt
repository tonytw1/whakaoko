package uk.co.eelpieconsulting.feedlistener

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class IntegrationTest  (@Autowired private val webClient: WebTestClient) {

    @Test
    fun shouldHaveHealthCheckEndpoint() {
        val exchange = webClient.get().uri("/healthz").exchange()
        exchange.expectStatus().isOk
        val body = String(exchange.expectBody().returnResult().responseBody!!)
        assertTrue(body.contains("ok"))
    }

}