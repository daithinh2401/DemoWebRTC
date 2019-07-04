package com.example.daithinh.prototypewebrtc.MySQLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.SQLiteConnection.User;
import com.example.daithinh.prototypewebrtc.Service.MyService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

/**
 * Created by Dai Thinh on 11/5/2017.
 */

public class InsertUser extends AsyncTask {


    ProgressDialog dialog;
    private static final String url = "http://thinhtdt.esy.es/webrtc/create.php";
    User user;
    Activity activity;
    String json = "";

    public InsertUser(Activity activity ,User user){
        this.activity = activity;
        this.user = user;
        dialog = new ProgressDialog(activity);
        dialog.setMessage("Loading, please wait.");
        dialog.show();
    }


    @Override
    protected Object doInBackground(Object[] objects) {

        MyService jsonParse = new MyService();
        // Tạo danh sách tham số gửi đến máy chủ
        ArrayList<NameValuePair> args = new ArrayList<>();
        args.add(new BasicNameValuePair("username", user.getUsername()));

        // Lấy đối tượng JSON
        json = jsonParse.makeService(url, MyService.POST, args);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        Toast.makeText(activity, "Sign up complete" , Toast.LENGTH_LONG).show();
    }
}
