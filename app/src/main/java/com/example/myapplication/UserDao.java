package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);


    @Query("SELECT * FROM user WHERE username LIKE :username AND " +
            "password LIKE :password LIMIT 1")
    User findByName(String username, String password);

    @Query("INSERT INTO user(first_name,last_name,username,password,userrole)" +
            "VALUES(:name,:lastname,:username,:password,:userrole)")
    void insert(String name,String lastname,String username,String password,String userrole );



    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);

}