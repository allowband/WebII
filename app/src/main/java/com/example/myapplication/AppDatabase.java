package com.example.myapplication;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import java.io.Serializable;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase implements Serializable {
    public abstract UserDao userDao();
}

