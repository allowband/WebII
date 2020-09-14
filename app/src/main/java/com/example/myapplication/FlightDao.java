package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.Flight;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface FlightDao {

    @NotNull
    @Query("SELECT * FROM flight")
    List<Flight> loadAll();

    @Query("INSERT INTO flight(title,bitmap,seatLimit,places,datum)" +
            "VALUES(:title,:bitmap,:seatLimit,:places,:datum)")
    void insert(String title, byte[] bitmap, int seatLimit, String places,String datum);

    @Query("SELECT * FROM flight WHERE title LIKE :title LIMIT 1")
    Flight findByTitle(String title);

    @Query("DELETE FROM flight WHERE title LIKE :title")
    void deleteByTitle(String title);

    @Insert
    void insertAll(Flight... flights);

    @Delete
    void delete(Flight flight);
}
