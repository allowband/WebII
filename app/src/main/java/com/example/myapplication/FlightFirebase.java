package com.example.myapplication;

public class FlightFirebase {
    public FlightFirebase() {
    }

    public FlightFirebase(String title, int seatLimit, String places, String datum, String bytes) {
        this.title = title;
        this.seatLimit = seatLimit;
        this.places = places;
        this.datum = datum;
        this.bytes = bytes;
    }

    public String title;

    public int seatLimit;

    public String places;

    public String datum;

    public String bytes;
}
