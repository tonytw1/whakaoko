package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Embedded

@Embedded
class Place {
    var address: String? = null
    var latLong: LatLong? = null

    constructor()
    constructor(address: String?, latLong: LatLong?) {
        this.address = address
        this.latLong = latLong
    }

    override fun toString(): String {
        return "Place{" +
                "address='" + address + '\'' +
                ", latLong=" + latLong +
                '}'
    }
}
