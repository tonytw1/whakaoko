package uk.co.eelpieconsulting.feedlistener.controllers.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
internal class TokenResponse {
    @JsonProperty("access_token")
    var accessToken: String? = null
}