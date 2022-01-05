package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Embedded

@Embedded
class Category {
    var value: String? = null
}