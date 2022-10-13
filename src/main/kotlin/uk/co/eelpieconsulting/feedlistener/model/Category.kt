package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Embedded

@Embedded
data class Category (var value: String? = null)