package com.example.myapplication;

public class ReserveFirebase {

    public ReserveFirebase() {
    }

    public ReserveFirebase(String flight, String user, int seat) {
        this.flight = flight;
        this.user = user;
        this.seat = seat;
    }

    public String flight;

    public String user;

    public int seat;
}
