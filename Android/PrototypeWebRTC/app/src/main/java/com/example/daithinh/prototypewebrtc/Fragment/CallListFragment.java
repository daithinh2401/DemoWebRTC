package com.example.daithinh.prototypewebrtc.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.Adapter.CustomAdapter;
import com.example.daithinh.prototypewebrtc.Manager.ScreenManager;
import com.example.daithinh.prototypewebrtc.Manager.SocketManager;
import com.example.daithinh.prototypewebrtc.Manager.VideoManager;
import com.example.daithinh.prototypewebrtc.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.ArrayList;

public class CallListFragment extends Fragment implements CustomAdapter.IViewHolderClick, VideoManager.SignalingObserver {

    Context mContext;
    View mRootView;
    RecyclerView listView;

    TextView textViewListUser;
    ArrayList<String> listUser = new ArrayList<>();
    CustomAdapter adapter;

    SocketManager mSocketManager;
    Socket socket;

    ScreenManager mScreenManager;

    LinearLayoutManager layoutManager;

    private String mMyId = "";
    private String mOtherId = "";

    VideoManager mVideoManager;

    public CallListFragment() {
    }

    @SuppressLint("ValidFragment")
    public CallListFragment(Context context) {
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.call_list_fragment, container, false);
        listView = mRootView.findViewById(R.id.listView);

        mScreenManager = ScreenManager.getInstance();
        mSocketManager = SocketManager.getInstance();
        socket = mSocketManager.getSocket();

        mVideoManager = VideoManager.getInstance();

        mMyId = mScreenManager.mSelfName;

        textViewListUser = mRootView.findViewById(R.id.textViewListUser);
        textViewListUser.setText(Html.fromHtml("Hello " + mMyId + " , choose your friends in list to make video call !"));

        adapter = new CustomAdapter(listUser, this);

        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mVideoManager.registerObserver(this);

        registerSocketCallback();
        getListUsers();
    }

    @Override
    public void onPause() {
        super.onPause();

        removeSocketCallback();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<String> parseListFromServer(JSONArray jsonArray) {
        ArrayList<String> list = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add((String) jsonArray.get(i));
            }
        } catch (JSONException e) {
            Log.e("TAG", "MyDemo.CallListFragment.parseListFromServer(): Failed , exception " + e.toString());
            list = null;
        }

        return list;
    }

    private void updateList(JSONArray jsonArray){
        final ArrayList<String> list = parseListFromServer(jsonArray);
        if(list != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listUser = list;
                    adapter.updateList(listUser);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void getListUsers() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", mMyId);
        } catch (JSONException e) {
            Log.e("TAG", "MyDemo.CallListFragment.getListUsers(): Failed , exception " + e.toString());
            object = null;
        }

        if (object != null) {
            mSocketManager.sendToSocket(SocketManager.SOCKET_MESSAGE_GET_USER, object);
        }
    }



    /* --------------- Socket listener --------------- */

    private Emitter.Listener onUpdateUserList = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            JSONArray jsonArray = (JSONArray) objects[0];
            updateList(jsonArray);
        }
    };

    private Emitter.Listener onOffer = new Emitter.Listener() {
        @Override
        public void call(final Object... objects) {
            final JSONObject object = (JSONObject) objects[0];
            String description = "";

            try {
                mOtherId = object.getString("send");
                description = object.getString(VideoManager.SDP);

            } catch (JSONException e) {}

            final SessionDescription remoteDescription = new SessionDescription(SessionDescription.Type.OFFER , description);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Call Video");
                    alertDialog.setMessage("Someone want to call you, accept ?");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE  , "YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // Accept video call, send answer with isAccept = true

                            mVideoManager.setRemoteDescription(remoteDescription);
                            mVideoManager.makeAnswer();
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE  , "NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // Accept video call, send answer with isAccept = false
                            sendReject();
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                }
            });
        }
    };

    private Emitter.Listener onAnswer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject object = (JSONObject) args[0];
            try {
                String description = object.getString(VideoManager.SDP);
                SessionDescription remoteDescription = new SessionDescription(SessionDescription.Type.ANSWER, description);

                mVideoManager.setRemoteDescription(remoteDescription);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onReject = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Call Fail");
                    alertDialog.setMessage("The other doesn't want to connect with you");

                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE  , "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();

                        }
                    });
                    alertDialog.show();
                }
            });
        }
    };

    private Emitter.Listener onCandidate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject object = (JSONObject) args[0];

                String sdp = object.getString(VideoManager.SDP);
                String sdpMid = object.getString(VideoManager.SDP_MID);
                int index = object.getInt(VideoManager.SDP_M_LINE_INDEX);


                IceCandidate iceCandidate = new IceCandidate(sdpMid, index, sdp);

                mVideoManager.addIceCandidate(iceCandidate);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    /* --------------- Socket listener --------------- */





    /* --------------- Send message to socket --------------- */
    private void sendOffer(SessionDescription sessionDescription){
        JSONObject object = new JSONObject();

        try {
            object.put("to", mOtherId);
            object.put("send", mMyId);
            object.put("receive", mOtherId);
            object.put(VideoManager.SDP, sessionDescription.description);
        } catch (Exception e) {
            Log.e("TAG", "CallListFragment.sendOffer(): Failed ! Error = " + e.toString() );
        }

        socket.emit(SocketManager.SOCKET_MESSAGE_OFFER, object);
    }

    private void sendAnswer(SessionDescription sessionDescription){
        JSONObject object = new JSONObject();

        try {
            object.put("to", mOtherId);
            object.put("send", mMyId);
            object.put("receive", mOtherId);
            object.put(VideoManager.SDP, sessionDescription.description);
        } catch (Exception e) {
            Log.e("TAG", "CallListFragment.sendOffer(): Failed ! Error = " + e.toString() );
        }

        socket.emit(SocketManager.SOCKET_MESSAGE_ANSWER, object);
    }

    private void sendReject() {
        try {
            JSONObject object = new JSONObject();
            object.put("to", mOtherId);
            object.put("send", mMyId);
            object.put("receive", mOtherId);

            socket.emit(SocketManager.SOCKET_MESSAGE_REJECT, object);
        } catch (Exception e) {
            Log.e("TAG", "CallListFragment.sendOffer(): Failed ! Error = " + e.toString() );
        }
    }

    private void sendIceCandidate(IceCandidate iceCandidate){
        try {
            JSONObject object = new JSONObject();
            object.put("to", mOtherId);
            object.put("send", mMyId);
            object.put("receive", mOtherId);

            object.put(VideoManager.SDP, iceCandidate.sdp);
            object.put(VideoManager.SDP_MID, iceCandidate.sdpMid);
            object.put(VideoManager.SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);

            socket.emit(SocketManager.SOCKET_MESSAGE_CANDIDATE, object);
        } catch (Exception e) {
            Log.e("TAG", "CallListFragment.sendOffer(): Failed ! Error = " + e.toString() );
        }
    }
    /* --------------- Send message to socket --------------- */




    @Override
    public void onItemClick(int position) {
        mOtherId = listUser.get(position);
        if(!mOtherId.equals(mMyId)) {

            mVideoManager.makeOffer();
        }
        else {
            Toast.makeText(mContext, "Cannot call your self", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerSocketCallback() {
        Socket socket = SocketManager.getInstance().getSocket();

        socket.on(SocketManager.SOCKET_MESSAGE_UPDATE_USER_LIST, onUpdateUserList);
        socket.on(SocketManager.SOCKET_MESSAGE_OFFER, onOffer);
        socket.on(SocketManager.SOCKET_MESSAGE_ANSWER, onAnswer);
        socket.on(SocketManager.SOCKET_MESSAGE_REJECT, onReject);
        socket.on(SocketManager.SOCKET_MESSAGE_CANDIDATE, onCandidate);
    }

    private void removeSocketCallback() {
        Socket socket = SocketManager.getInstance().getSocket();

        socket.off(SocketManager.SOCKET_MESSAGE_UPDATE_USER_LIST, onUpdateUserList);
        socket.off(SocketManager.SOCKET_MESSAGE_OFFER, onOffer);
        socket.on(SocketManager.SOCKET_MESSAGE_ANSWER, onAnswer);
        socket.on(SocketManager.SOCKET_MESSAGE_REJECT, onReject);
        socket.on(SocketManager.SOCKET_MESSAGE_CANDIDATE, onCandidate);
    }

    @Override
    public void onSdpObserveCreateSuccess(SessionDescription sessionDescription) {

        if(sessionDescription.type == SessionDescription.Type.OFFER)
        {
            sendOffer(sessionDescription);
        }
        else if(sessionDescription.type == SessionDescription.Type.ANSWER)
        {
            sendAnswer(sessionDescription);
        }
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        sendIceCandidate(iceCandidate);
    }

    @Override
    public void onAddStream() {
        mScreenManager.openScreen(ScreenManager.SCREEN_TYPE_VIDEO_CALL);
    }
}
