package com.example.myapplication;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import java.io.Serializable;

@Database(entities = {Reservation.class}, version = 1)
public abstract class ReserveDatabase extends RoomDatabase implements Serializable {
    public abstract ReserveDao reserveDao();
}
