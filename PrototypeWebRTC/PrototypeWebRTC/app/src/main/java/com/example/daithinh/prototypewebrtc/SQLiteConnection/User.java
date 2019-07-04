package com.example.daithinh.prototypewebrtc.SQLiteConnection;

/**
 * Created by Dai Thinh on 11/4/2017.
 */

public class User {
    private String username;

    public User(){}

    public User(String username){
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
