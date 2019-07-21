package com.example.daithinh.prototypewebrtc.Manager;


import com.example.daithinh.prototypewebrtc.View.IMainView;

public class ScreenManager {

    public static final int SCREEN_TYPE_LOGIN             = 1;
    public static final int SCREEN_TYPE_LIST_USER         = 2;
    public static final int SCREEN_TYPE_VIDEO_CALL        = 3;

    public String mSelfName = "";
    private static ScreenManager mInstance;
    private IMainView mainView;

    private ScreenManager(){
    }

    public static ScreenManager getInstance(){
        if(mInstance == null){
            mInstance = new ScreenManager();
        }

        return mInstance;
    }

    public void registerMainView(IMainView mainView){
        this.mainView = mainView;
    }

    public void removeMainView(){
        this.mainView = null;
    }

    public void openScreen(int screenType){
        mainView.openScreen(screenType);
    }

    public void showJoinFailed(String message){
        mainView.showToast(message);
    }

}
