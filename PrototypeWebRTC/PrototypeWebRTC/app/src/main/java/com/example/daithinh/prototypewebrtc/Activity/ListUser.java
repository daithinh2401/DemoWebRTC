package com.example.daithinh.prototypewebrtc.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.daithinh.prototypewebrtc.Adapter.CustomAdapter;
import com.example.daithinh.prototypewebrtc.R;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.User;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.UserManager;
import com.example.daithinh.prototypewebrtc.Service.MyService;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class ListUser extends Activity{

    private static final String SIGNALINGSERVERURL = "https://testserver-webrtc.herokuapp.com";
    TextView textViewListUser;
    ListView listView;
    ArrayList<User> listUser = new ArrayList<>();
    String myName;
    CustomAdapter adapter;
    Button btnRefresh;
    UserManager manager;
    static Socket socket;

    public static Socket getSocket() {
        return socket;
    }

    public void connectToSocket(){
        try {
            socket = IO.socket(SIGNALINGSERVERURL);
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        connectToSocket();

        Intent intent = getIntent();
        myName = intent.getStringExtra("MyName");



        textViewListUser = findViewById(R.id.textViewListUser);
        listView = findViewById(R.id.listView);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(onRefresh);
        textViewListUser.setText(Html.fromHtml("Hello "  + "<b>" + myName.toUpperCase() + "</b>" + " , choose your friends in list to make video call !"));

        joinToSocket();

        manager = new UserManager(getApplicationContext());
        listUser = manager.getAllUser();
        removeUserInList(listUser , myName);


        adapter = new CustomAdapter(getApplicationContext() , R.layout.listitem , listUser);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                JSONObject object = new JSONObject();
                String otherId = listUser.get(position).getUsername();
                if(!otherId.equals(myName)) {

                    try {
                        object.put("id", otherId);
                        object.put("caller" , myName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent it = new Intent(ListUser.this, CallActivity.class);

                    startActivity(it);

                    socket.emit("send", object);
                }
                else Toast.makeText(getApplicationContext() , "Cannot call your self" , Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext() , listUser.get(position).getUsername() , Toast.LENGTH_SHORT).show();


            }


        });

        socket.on("wantconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                ListUser.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject object = (JSONObject)args[0];
                        final JSONObject jsonObject = new JSONObject();
                        String otherId = "";
                        String caller = "";
                        try {
                            otherId = object.getString("id");
                            caller = object.getString("caller");
                            jsonObject.put("id" , otherId);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(ListUser.this).create();
                        alertDialog.setTitle("Call Video");
                        alertDialog.setMessage(caller + " want to call you, accept ?");

                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE  , "YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                socket.emit("acceptconnect" , jsonObject);

                                Intent it = new Intent(ListUser.this , CallActivity.class);
                                startActivity(it);


                            }
                        });

                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE  , "NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                socket.emit("unacceptconnect" , jsonObject);

                            }
                        });

                        alertDialog.show();
                    }
                });



            }
        });

        socket.on("userjoin" , new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                ListUser.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject object = (JSONObject)args[0];
                        try {
                            String name = object.getString("id");
                            Toast.makeText(getApplicationContext() , name + " has online !!" , Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });


            }
        });

    }


    public void removeUserInList(ArrayList<User> list , String username){
        for(int i = 0 ; i < list.size(); i ++){
            if(list.get(i).getUsername().equals(username)){
                list.remove(i);
            }
        }

    }


    public void joinToSocket(){
        JSONObject object = new JSONObject();
        try {
            object.put("id", myName);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("join" , object);

    }





    View.OnClickListener onRefresh = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
             manager.update();
             getUser();


        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        connectToSocket();
        joinToSocket();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void getUser(){
            new OnResetButton(this, listView, myName).execute(listUser);

    }

}

class OnResetButton extends AsyncTask<ArrayList<User> , Void, Void> {

    User user;
    ArrayList<User> listUser = new ArrayList<>();
    Activity activity;
    ProgressDialog dialog;
    ListView listView;
    String myName;
    UserManager manager;

    public OnResetButton(Activity activity, ListView listView, String myName) {
        dialog = new ProgressDialog(activity);
        this.myName = myName;
        this.activity = activity;
        this.listView = listView;
        manager = new UserManager(activity.getApplicationContext());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Loading, please wait.");
        dialog.show();
    }

    @Override
    protected Void doInBackground(ArrayList<User>... params) {
        listUser = params[0];
        listUser.clear();

        MyService jsonParser = new MyService();
        String json = jsonParser.callService("http://thinhtdt.esy.es/webrtc/display.php", MyService.GET);


        if (json != null) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                if (jsonObj != null) {
                    JSONArray jsonArray = jsonObj.getJSONArray("user_info");


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        try {
                            user = new User(obj.getString("username"));
                            listUser.add(user);
                            manager.Insert(user);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("JSON Data", "Didn't receive any data from server!");
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                removeUserInList(listUser , myName);
                listView.setAdapter(new CustomAdapter(activity.getApplicationContext(), R.layout.listitem, listUser));

            }
        });

    }

    public void removeUserInList(ArrayList<User> list , String username){
        for(int i = 0 ; i < list.size(); i ++){
            if(list.get(i).getUsername().equals(username)){
                list.remove(i);
            }
        }

    }

}






