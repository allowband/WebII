package com.example.myapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;

@Entity

public class Reservation implements Serializable{

    @PrimaryKey
    public int rid;

    @ColumnInfo(name = "flight")
    public String flight;

    @ColumnInfo(name = "user")
    public String user;

    @ColumnInfo(name= "seat")
    public int seat;
}
