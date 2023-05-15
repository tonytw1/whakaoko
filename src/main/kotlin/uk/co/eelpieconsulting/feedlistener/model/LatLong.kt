package uk.co.eelpieconsulting.feedlistener.model

import dev.morphia.annotations.Embedded

@Embedded
class LatLong {
    var latitude = 0.0
        private set
    var longitude = 0.0
        private set

    constructor()
    constructor(latitude: Double, longitude: Double) {
        require(!(latitude < -90 || latitude > 90)) { "Latitude is out of bounds" }
        require(!(longitude < -180 || longitude > 180)) { "Longitude is out of bounds" }
        this.latitude = latitude
        this.longitude = longitude
    }

    override fun toString(): String {
        return "LatLong [latitude=$latitude, longitude=$longitude]"
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(latitude)
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(longitude)
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as LatLong
        if (java.lang.Double.doubleToLongBits(latitude) != java.lang.Double
                .doubleToLongBits(other.latitude)
        ) return false
        return if (java.lang.Double.doubleToLongBits(longitude) != java.lang.Double
                .doubleToLongBits(other.longitude)
        ) false else true
    }
}