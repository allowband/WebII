package com.example.myapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Flight implements Serializable {
    @PrimaryKey
    public int fid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "seatLimit")
    public int seatLimit;

    @ColumnInfo(name= "places")
    String places;

    @ColumnInfo(name= "datum")
    String datum;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    byte[] bitmap;
}
