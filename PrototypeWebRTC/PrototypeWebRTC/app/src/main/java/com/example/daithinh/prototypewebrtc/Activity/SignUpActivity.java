package com.example.daithinh.prototypewebrtc.Activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.MySQLConnection.InsertUser;
import com.example.daithinh.prototypewebrtc.R;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.User;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.UserManager;
import com.example.daithinh.prototypewebrtc.Service.MyService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;


/**
 * Created by Dai Thinh on 11/4/2017.
 */

public class SignUpActivity extends Activity{

    EditText editTextSignUp;
    Button btnSignUp, btnCancel;
    UserManager manager;
    String username;
    ArrayList<User> listUser = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        manager = new UserManager(getApplicationContext());

        editTextSignUp = findViewById(R.id.editText_SignUp);
        btnSignUp = findViewById(R.id.btn_Accept_SignUp);
        btnCancel = findViewById(R.id.btn_Cancel_SignUp);

        btnCancel.setOnClickListener(onCancel);
        btnSignUp.setOnClickListener(onSignUp);

    }

    View.OnClickListener onSignUp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            username = editTextSignUp.getText().toString();
            listUser = manager.getAllUser();

            if(signUpNewUser(listUser , username)){
                User user = new User(username);
                insertDatabase(user);
                manager.Insert(user);
            }
            else {
                Toast.makeText(getApplicationContext(), "This name has already sign , try another name" , Toast.LENGTH_SHORT).show();
            }



        }
    };

    View.OnClickListener onCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            android.os.Process.killProcess(android.os.Process.myPid());

        }
    };


    public void insertDatabase(User user){
        new InsertUser(this , user).execute();
    }

    public boolean signUpNewUser(ArrayList<User> list, String name){
        for(User u : list){
            if(u.getUsername().equals(name)){
                return false;
            }
        }
        return true;
    }

}




