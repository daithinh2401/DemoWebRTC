package com.example.daithinh.prototypewebrtc.MySQLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.Adapter.CustomAdapter;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.User;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.UserManager;
import com.example.daithinh.prototypewebrtc.Service.MyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Dai Thinh on 11/5/2017.
 */

public class GetUser extends AsyncTask {

    private User user;
    Activity activity;
    UserManager manager;
    ProgressDialog dialog;
    ArrayList<User> list = new ArrayList<>();





    public GetUser(Activity activity){
        this.activity = activity;
        dialog = new ProgressDialog(activity);
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Loading, please wait.");
        dialog.show();
    }

    public boolean haveInList(ArrayList<User> list, User user){
        for(User u : list){
            if(u.getUsername().equals(user.getUsername())) return true;
        }
        return false;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        manager = new UserManager(activity.getApplicationContext());
        list = manager.getAllUser();


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
                            if(!haveInList(list , user)){
                                manager.Insert(user);
                            }


                        }catch (Exception e){
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
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }


        Toast.makeText(activity.getApplicationContext() , "Update success !" ,Toast.LENGTH_SHORT).show();


    }

}
