package com.example.daithinh.prototypewebrtc.Manager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketManager {

//    private static final String SIGNALING_SERVER_URL = "https://testserver-webrtc.herokuapp.com";
    private static final String SIGNALING_SERVER_URL = "http://192.168.1.144:3000";

    // For call
    public static final String SOCKET_MESSAGE_OFFER                         = "offer";
    public static final String SOCKET_MESSAGE_ANSWER                        = "answer";
    public static final String SOCKET_MESSAGE_REJECT                        = "reject";
    public static final String SOCKET_MESSAGE_CANDIDATE                     = "candidate";


    // For another
    public static final String SOCKET_MESSAGE_JOIN                          = "join";
    public static final String SOCKET_MESSAGE_JOIN_SUCCESS                  = "join_success";
    public static final String SOCKET_MESSAGE_UPDATE_USER_LIST		        = "update_user_list";

    public static final String SOCKET_MESSAGE_GET_USER					    = "get_users";


    private Socket mSocket;
    private static SocketManager mInstance;

    public static  SocketManager getInstance(){
        if(mInstance == null){
            mInstance = new SocketManager();
        }
        return mInstance;
    }

    public SocketManager(){
        connectToSocket();
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void connectToSocket(){
        try {
            mSocket = IO.socket(SIGNALING_SERVER_URL);
            mSocket.connect();
            Log.d("TAG" , "MyDemo.SocketManager.connectToSocket(): Success !");
        } catch (Exception e) {
            Log.e("TAG" , "MyDemo.SocketManager.connectToSocket(): Failed when connect socket, exception = " + e.toString());
        }
    }

    public void disconnectSocket(){
        mSocket.disconnect();
    }


    // Send message to socket
    public void sendToSocket(String message, JSONObject jsonObject){
        Log.d("TAG" , "MyDemo.SocketManager.sendToSocket(): message = " + message + " jsonObject = " + jsonObject);
        mSocket.emit(message , jsonObject);
    }


    public void doLogIn(String name){
        JSONObject jsonObject = createLoginObject(name);
        if(jsonObject != null)
            SocketManager.getInstance().sendToSocket("join", jsonObject);
    }

    private JSONObject createLoginObject(String name){
        JSONObject object = new JSONObject();

        try {
            object.put("id", name);
        }
        catch (JSONException e) {
            Log.e("TAG" , "SocketManager.joinToSocket(): Failed , exception " + e.toString());
            object = null;
        }

        return object;
    }
}
