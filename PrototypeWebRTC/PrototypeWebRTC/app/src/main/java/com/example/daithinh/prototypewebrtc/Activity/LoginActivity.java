package com.example.daithinh.prototypewebrtc.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.MySQLConnection.GetUser;
import com.example.daithinh.prototypewebrtc.R;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.User;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.UserManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.PropertyPermission;

public class LoginActivity extends Activity {

    EditText editTextLogin;
    Button btnLogin , btnSignUp;
    String myName = "";
    UserManager manager;
    ArrayList<User> listUser = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextLogin = findViewById(R.id.editText_Login);
        btnLogin = findViewById(R.id.btn_Login);
        btnSignUp = findViewById(R.id.btn_SignUp);



        btnSignUp.setOnClickListener(onSignUp);
        btnLogin.setOnClickListener(onLogIn);

        manager = new UserManager(getApplicationContext());
        getUserFromDatabase();
        listUser = manager.getAllUser();

    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserFromDatabase();
        listUser = manager.getAllUser();


    }

    View.OnClickListener onSignUp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(LoginActivity.this , SignUpActivity.class);
            startActivity(intent);

        }
    };

    View.OnClickListener onLogIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            myName = editTextLogin.getText().toString();
            if(checkLogin(myName)){
                Intent intent = new Intent(LoginActivity.this , ListUser.class);
                intent.putExtra("MyName" , myName);
                startActivity(intent);

            }
            else {
                Toast.makeText(getApplicationContext() , "Your name have not exist, please sign up first !" , Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void getUserFromDatabase(){
        new GetUser(this).execute();

    }

    public boolean checkLogin(String name){
        ArrayList<User> list = new ArrayList<>();
        list = manager.getAllUser();
        for(User u : list){
            if(u.getUsername().equals(name)){
                return true;
            }
        }
        return false;
    }





}
