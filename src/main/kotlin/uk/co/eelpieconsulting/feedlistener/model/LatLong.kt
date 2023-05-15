package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Entity

@Entity
class LatLong(val latitude: Double, val longitude: Double) {

    init {
        require(!(latitude < -90 || latitude > 90))
        { "Latitude is out of bounds" }
        require(!(longitude < -180 || longitude > 180))
        { "Longitude is out of bounds" }
    }

    override fun toString(): String {
        return "LatLong [latitude=$latitude, longitude=$longitude]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatLong

        if (latitude != other.latitude) return false
        return longitude == other.longitude
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

}