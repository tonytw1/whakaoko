package uk.co.eelpieconsulting.feedlistener.controllers.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
internal class TokenInfo {
    @JsonProperty("user_id")
    var userId: String? = null
    var email: String? = null
}