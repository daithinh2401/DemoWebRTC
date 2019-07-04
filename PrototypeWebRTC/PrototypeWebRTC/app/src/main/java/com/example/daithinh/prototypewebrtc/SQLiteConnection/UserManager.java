package com.example.daithinh.prototypewebrtc.SQLiteConnection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Dai Thinh on 11/4/2017.
 */

public class UserManager extends SQLiteOpenHelper {

    public UserManager(Context context) {
        super(context, "UserInfo", null, 1);
    }

    public boolean Insert(User user){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Username" , user.getUsername());
        long id = db.insert("User" , null , values);
        return (id != -1);
    }


    public void update(){
        SQLiteDatabase db = getWritableDatabase();

        String delete = "drop table if exists User";
        String create = "create table User(Username TEXT PRIMARY KEY)";
        db.execSQL(delete);
        db.execSQL(create);
    }


    public ArrayList<User> getAllUser(){
        ArrayList<User> listUser = new ArrayList<>();
        String sql = "select * from User";

        // b1. Lay db
        SQLiteDatabase db = getReadableDatabase();

        // b2. Thuc hien truy van, tra ve du lieu tho
        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext()) {
            String username = c.getString(0);


            User user = new User(username);
            listUser.add(user);
        }
        return listUser;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table User(Username TEXT PRIMARY KEY)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "delete table if exists User";
        db.execSQL(sql);
        onCreate(db);
    }

}
