package uk.co.eelpieconsulting.feedlistener.model;

import dev.morphia.annotations.Embedded;

@Embedded
public class Place {

    private String address;
    private LatLong latLong;

    public Place() {
    }

    public Place(String address, LatLong latLong) {
        this.address = address;
        this.latLong = latLong;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLong getLatLong() {
        return latLong;
    }

    public void setLatLong(LatLong latLong) {
        this.latLong = latLong;
    }

    @Override
    public String toString() {
        return "Place{" +
                "address='" + address + '\'' +
                ", latLong=" + latLong +
                '}';
    }
}

