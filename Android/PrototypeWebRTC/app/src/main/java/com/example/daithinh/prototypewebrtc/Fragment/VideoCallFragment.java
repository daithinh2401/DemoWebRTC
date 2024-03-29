package com.example.daithinh.prototypewebrtc.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.Manager.ScreenManager;
import com.example.daithinh.prototypewebrtc.Manager.SocketManager;
import com.example.daithinh.prototypewebrtc.Manager.VideoManager;
import com.example.daithinh.prototypewebrtc.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

public class VideoCallFragment extends Fragment {

    // Video renderer
    private VideoRenderer otherPeerRenderer;

    private static final String SDP_MID = "sdpMid";
    private static final String SDP_M_LINE_INDEX = "sdpMLineIndex";
    private static final String SDP = "sdp";

    private static final String CREATEOFFER = "createoffer";
    private static final String OFFER = "offer";
    private static final String ANSWER = "answer";
    private static final String CANDIDATE = "candidate";
    private static final String UNACCEPT = "unacceptconnect";

    private boolean createOffer = false;

    SocketManager mSocketManager;
    Socket socket;

    VideoManager mVideoManager;

    View mRootView;

    Context mContext;

    public VideoCallFragment(){}

    @SuppressLint("ValidFragment")
    public VideoCallFragment(Context context){
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.video_call_fragment, container, false);

        initView();

        return mRootView;
    }

    private void initView(){
        mSocketManager = SocketManager.getInstance();
        socket = mSocketManager.getSocket();

        mVideoManager = VideoManager.getInstance();

        // init video view
        GLSurfaceView videoView = mRootView.findViewById(R.id.fullscreenView);
        VideoRendererGui.setView(videoView, null);

        try {
            otherPeerRenderer = VideoRendererGui.createGui(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            VideoRenderer renderer = VideoRendererGui.createGui(80, 80, 20, 20, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            mVideoManager.renderVideoTrack(renderer);

        } catch (Exception e) {
            e.printStackTrace();
        }

        for(VideoTrack videoTrack : mVideoManager.getRemoteMediaStream().videoTracks){
            videoTrack.addRenderer(otherPeerRenderer);
        }

    }



//    Emitter.Listener onCreateOffer = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            createOffer = true;
//            peerConnection.createOffer(sdpObserver , new MediaConstraints());
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext, "Received create offer" , Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    };
//
//
//
//    Emitter.Listener onOffer = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//
//            try {
//                JSONObject obj = (JSONObject) args[0];
//                SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, obj.getString(SDP));
//                peerConnection.setRemoteDescription(sdpObserver, sdp);
//                peerConnection.createAnswer(sdpObserver, new MediaConstraints());
//            }
//            catch (JSONException e) {
//                e.printStackTrace();
//            }
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext, "Received offer" , Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    };
//
//    Emitter.Listener onAnswer = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            try {
//                JSONObject obj = (JSONObject) args[0];
//                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
//                        obj.getString(SDP));
//                peerConnection.setRemoteDescription(sdpObserver, sdp);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext, "Received answer" , Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    };
//
//    Emitter.Listener onCandidate = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            try {
//                JSONObject obj = (JSONObject) args[0];
//
//                peerConnection.addIceCandidate(
//                        new IceCandidate(obj.getString(SDP_MID),
//                        obj.getInt(SDP_M_LINE_INDEX),
//                        obj.getString(SDP)));
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext, "Received candidate" , Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    };
//
//    private JSONObject createLeaveRoomMessage(String name){
//        JSONObject object = new JSONObject();
//
//        try {
//            object.put("id", name);
//        }
//        catch (JSONException e) {
//            Log.e("TAG" , "MyDemo.MainActivity.createLeaveRoomMessage(): Failed , exception " + e.toString());
//            object = null;
//        }
//
//        return object;
//    }
//
//    private void leaveRoom(){
//        String name = ScreenManager.getInstance().mSelfName;
//        Log.d("TAG" , "MyDemo.MainActivity.disconnectSocket(): Send left room" + name);
//
//        if(!TextUtils.isEmpty(name)){
//            Log.d("TAG" , "MyDemo.MainActivity.disconnectSocket(): Send left room");
//            JSONObject jsonObject = createLeaveRoomMessage(name);
//
//            if(jsonObject != null){
//                mSocketManager.sendToSocket("leave", jsonObject);
//            }
//        }
//    }
//
//    public void cancelConnect(){
//        peerConnection.removeStream(localMediaStream);
//        peerConnection.close();
//
//        leaveRoom();
//
//        socket.disconnect();
//        socket.close();
//        android.os.Process.killProcess(android.os.Process.myPid());
//    }
//
//    SdpObserver sdpObserver = new SdpObserver() {
//        @Override
//        public void onCreateSuccess(final SessionDescription sessionDescription) {
//            peerConnection.setLocalDescription(sdpObserver, sessionDescription);
//
//            try {
//                JSONObject obj = new JSONObject();
//                obj.put(SDP, sessionDescription.description);
//                if (createOffer) {
//                    socket.emit(OFFER, obj);
//                } else {
//                    socket.emit(ANSWER, obj);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        @Override
//        public void onSetSuccess() {
//
//        }
//
//        @Override
//        public void onCreateFailure(String s) {
//
//        }
//
//        @Override
//        public void onSetFailure(String s) {
//
//        }
//    };
//
//
//    PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
//        @Override
//        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
//            Log.d("RTCAPP", "onSignalingChange:" + signalingState.toString());
//        }
//
//        @Override
//        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//            Log.d("RTCAPP", "onIceConnectionChange:" + iceConnectionState.toString());
//            if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED){
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
//                        alertDialog.setTitle("DISCONNECT");
//                        alertDialog.setMessage(" Your call has been cancel ! ");
//
//                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE  , "OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                cancelConnect();
//
//                            }
//                        });
//
//                        alertDialog.show();
//
//                    }
//                });
//
//            }
//
//        }
//
//        @Override
//        public void onIceConnectionReceivingChange(boolean b) {
//
//        }
//
//        @Override
//        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
//
//        }
//
//        @Override
//        public void onIceCandidate(IceCandidate iceCandidate) {
//            try {
//                JSONObject obj = new JSONObject();
//                obj.put(SDP_MID, iceCandidate.sdpMid);
//                obj.put(SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);
//                obj.put(SDP, iceCandidate.sdp);
//                socket.emit(CANDIDATE, obj);
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onAddStream(MediaStream mediaStream) {
//            mediaStream.videoTracks.getFirst().addRenderer(otherPeerRenderer);
//
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext , "Add stream" , Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        }
//
//        @Override
//        public void onRemoveStream(MediaStream mediaStream) {
//
//            cancelConnect();
//
//        }
//
//        @Override
//        public void onDataChannel(DataChannel dataChannel) {
//
//        }
//
//        @Override
//        public void onRenegotiationNeeded() {
//
//        }
//    };
}
