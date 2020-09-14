package com.example.myapplication;

public class UserFirebase {
    public UserFirebase() {
    }

    public UserFirebase(String firstname, String lastname, String username, String password, String userrole) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
        this.userrole = userrole;

    }

    public String firstname;

    public String lastname;

    public String username;

    public String password;

    public String userrole;
}
