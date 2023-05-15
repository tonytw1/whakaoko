package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Embedded

@Embedded
class Place constructor(val address: String?, val latLong: LatLong?) {

    override fun toString(): String {
        return "Place{" +
                "address='" + address + '\'' +
                ", latLong=" + latLong +
                '}'
    }
}
