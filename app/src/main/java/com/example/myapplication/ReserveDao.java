package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.firebase.database.annotations.NotNull;

import java.util.List;

@Dao
public interface ReserveDao {

    @NotNull
    @Query("SELECT * FROM reservation WHERE user LIKE :user ")
    List<Reservation> reservationUser(String user);

    @NotNull
    @Query("SELECT * FROM reservation WHERE flight LIKE :flight ")
    List<Reservation> reservationFlight(String flight);


    @Query("SELECT * FROM reservation WHERE flight LIKE :flight AND user LIKE :user")
    List<Reservation> findByMatch(String flight,String user);

    @Query("SELECT * FROM reservation WHERE seat LIKE :seat AND flight LIKE :flight")
    Reservation findBySeat(String flight,int seat);

    @Query("SELECT * FROM reservation WHERE seat LIKE :seat AND flight LIKE :flight AND user LIKE :user")
    Reservation findMySeat(String flight,String user,int seat);

    @Query("DELETE FROM reservation WHERE flight LIKE :flight")
    void deleteByFlight(String flight);

    @Query("DELETE FROM reservation WHERE flight LIKE :flight AND user LIKE :user AND seat LIKE :seat")
    void deleteByUser(String flight,String user,int seat);

    @Query("INSERT INTO reservation(flight,user,seat)" +
            "VALUES(:flight,:user,:seat)")
    void insert(String flight,String user,int seat);

    @Insert
    void insertAll(Reservation... reserve);

    @Delete
    void delete(Reservation reserve);
}
