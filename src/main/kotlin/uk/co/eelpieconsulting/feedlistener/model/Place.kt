package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity

@Entity
class Place(val address: String?, val latLong: LatLong?) {

    override fun toString(): String {
        return "Place{" +
                "address='" + address + '\'' +
                ", latLong=" + latLong +
                '}'
    }
}
