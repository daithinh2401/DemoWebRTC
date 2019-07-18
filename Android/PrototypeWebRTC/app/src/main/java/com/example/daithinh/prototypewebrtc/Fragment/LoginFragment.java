package com.example.daithinh.prototypewebrtc.Fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.daithinh.prototypewebrtc.Manager.ScreenManager;
import com.example.daithinh.prototypewebrtc.Manager.SocketManager;
import com.example.daithinh.prototypewebrtc.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String JOIN_MESSAGE         = "join";
    private static final String JOIN_SUCCESS         = "join_success";
    private static final String JOIN_FAILED          = "join_faied";

    Context mContext;

    EditText editTextLogin;
    Button btnLogin;
    View mRootView;

    public LoginFragment(){}

    @SuppressLint("ValidFragment")
    public LoginFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.login_fragment, container, false);

        editTextLogin = mRootView.findViewById(R.id.editText_Login);
        btnLogin = mRootView.findViewById(R.id.btn_Login);
        btnLogin.setOnClickListener(this);

        return mRootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_Login:
                doLogIn(editTextLogin.getText().toString());
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        removeLoginCallback();
    }

    @Override
    public void onResume() {
        super.onResume();

        registerLoginCallback();
    }

    private void doLogIn(String name){
        JSONObject jsonObject = createLoginObject(name);
        if(jsonObject != null)
            SocketManager.getInstance().sendToSocket(JOIN_MESSAGE, jsonObject);
    }

    public JSONObject createLoginObject(String name){
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

    private void registerLoginCallback(){
        Socket socket = SocketManager.getInstance().getSocket();

        socket.on(JOIN_SUCCESS, onJoinSuccess);
        socket.on(JOIN_FAILED, onJoinFailed);
    }

    private void removeLoginCallback(){
        Socket socket = SocketManager.getInstance().getSocket();

        socket.off(JOIN_SUCCESS, onJoinSuccess);
        socket.off(JOIN_FAILED, onJoinFailed);
    }

    public Emitter.Listener onJoinSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            Log.d("TAG", "MyDemo.SocketManager.setSocketCallback(): onJoinSuccess !");
            ScreenManager.getInstance().mSelfName = editTextLogin.getText().toString();
            ScreenManager.getInstance().openScreen(ScreenManager.SCREEN_TYPE_LIST_USER);
        }
    };

    public Emitter.Listener onJoinFailed = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            Log.d("TAG", "MyDemo.SocketManager.setSocketCallback(): onJoinFailed !");
            ScreenManager.getInstance().showJoinFailed("User existed !");
        }
    };

}
