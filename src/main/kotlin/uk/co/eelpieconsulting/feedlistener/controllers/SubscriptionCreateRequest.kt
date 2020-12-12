package uk.co.eelpieconsulting.feedlistener.controllers

data class SubscriptionCreateRequest (val url: String, val channel: String, val name: String?)