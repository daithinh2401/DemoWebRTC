package com.example.daithinh.prototypewebrtc.Activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.Fragment.CallListFragment;
import com.example.daithinh.prototypewebrtc.Fragment.LoginFragment;
import com.example.daithinh.prototypewebrtc.Fragment.VideoCallFragment;
import com.example.daithinh.prototypewebrtc.Manager.ScreenManager;
import com.example.daithinh.prototypewebrtc.Manager.SocketManager;
import com.example.daithinh.prototypewebrtc.R;
import com.example.daithinh.prototypewebrtc.View.IMainView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements IMainView {

    FrameLayout mainLayout;
    FragmentManager mFragmentManager;
    Fragment mFragment;

    ScreenManager mScreenManager;
    SocketManager mSocketManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initView();
        openScreen(ScreenManager.SCREEN_TYPE_LOGIN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mScreenManager.registerMainView(this);
        if(!TextUtils.isEmpty(mScreenManager.mSelfName)) {
            mSocketManager.connectToSocket();
            mSocketManager.doLogIn(mScreenManager.mSelfName);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mScreenManager.removeMainView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        disconnectSocket();
    }

    @Override
    public void initView() {
        // find view
        mainLayout = findViewById(R.id.main_layout);

        // init manager for all screen
        mScreenManager = ScreenManager.getInstance();
        mSocketManager = SocketManager.getInstance();
        mFragmentManager = getFragmentManager();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void openScreen(int screenType) {
        switch (screenType){
            case ScreenManager.SCREEN_TYPE_LOGIN:
                mFragment = new LoginFragment(this);
                break;

            case ScreenManager.SCREEN_TYPE_LIST_USER:
                mFragment = new CallListFragment(this);
                break;

            case ScreenManager.SCREEN_TYPE_VIDEO_CALL:
                mFragment = new VideoCallFragment(this);
                break;
        }

        mFragmentManager.beginTransaction().replace(R.id.main_layout , mFragment).commit();
    }


    private JSONObject createLeaveRoomMessage(String name){
        JSONObject object = new JSONObject();

        try {
            object.put("id", name);
        }
        catch (JSONException e) {
            Log.e("TAG" , "MyDemo.MainActivity.createLeaveRoomMessage(): Failed , exception " + e.toString());
            object = null;
        }

        return object;
    }

    private void disconnectSocket(){
        String name = mScreenManager.mSelfName;
        Log.d("TAG" , "MyDemo.MainActivity.disconnectSocket(): Send left room" + name);

        if(!TextUtils.isEmpty(name)){
            Log.d("TAG" , "MyDemo.MainActivity.disconnectSocket(): Send left room");
            JSONObject jsonObject = createLeaveRoomMessage(name);

            if(jsonObject != null){
                mSocketManager.sendToSocket("leave", jsonObject);
            }
        }

        mSocketManager.disconnectSocket();
    }
}
