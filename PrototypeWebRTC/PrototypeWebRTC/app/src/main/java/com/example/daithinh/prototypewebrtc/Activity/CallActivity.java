package com.example.daithinh.prototypewebrtc.Activity;


import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

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


public class CallActivity extends AppCompatActivity {

    private PeerConnectionFactory peerConnectionFactory;
    private VideoSource localVideoSource;
    private MediaStream localMediaStream;
    private VideoRenderer otherPeerRenderer;

    private static final String VIDEO_TRACK_ID = "video1";
    private static final String AUDIO_TRACK_ID = "audio1";
    private static final String LOCAL_STREAM_ID = "stream1";

    private static final String SDP_MID = "sdpMid";
    private static final String SDP_M_LINE_INDEX = "sdpMLineIndex";
    private static final String SDP = "sdp";

    private static final String CREATEOFFER = "createoffer";
    private static final String OFFER = "offer";
    private static final String ANSWER = "answer";
    private static final String CANDIDATE = "candidate";
    private static final String UNACCEPT = "unacceptconnect";

    private boolean createOffer = false;

    private PeerConnection peerConnection;
    Socket socket;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call);


        socket = ListUser.getSocket();


        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        PeerConnectionFactory.initializeAndroidGlobals(
                this,  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true,  // Hardware Acceleration Enabled
                null); // Render EGL Context

        peerConnectionFactory = new PeerConnectionFactory();

        VideoCapturerAndroid vc = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfFrontFacingDevice(), null);

        localVideoSource = peerConnectionFactory.createVideoSource(vc, new MediaConstraints());
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
        localVideoTrack.setEnabled(true);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);

        localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID);
        localMediaStream.addTrack(localVideoTrack);
        localMediaStream.addTrack(localAudioTrack);



        GLSurfaceView videoView = (GLSurfaceView) findViewById(R.id.fullscreenView);

        VideoRendererGui.setView(videoView, null);
        try {
            otherPeerRenderer = VideoRendererGui.createGui(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            VideoRenderer renderer = VideoRendererGui.createGui(80, 80, 20, 20, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            localVideoTrack.addRenderer(renderer);

        } catch (Exception e) {
            e.printStackTrace();
        }


        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("turn:numb.viagenie.ca" , "ndthinh.tma@gmail.com" , "ndthinhwebrtc"));


        peerConnection = peerConnectionFactory.createPeerConnection(
                iceServers,
                new MediaConstraints(),
                peerConnectionObserver);

        peerConnection.addStream(localMediaStream);


        socket.on(UNACCEPT , onUnAccept);
        socket.on(CREATEOFFER, onCreateOffer);
        socket.on(OFFER, onOffer);
        socket.on(ANSWER, onAnswer);
        socket.on(CANDIDATE, onCandidate);



    }




    @Override
    protected void onPause() {
        super.onPause();

    }

    Emitter.Listener onUnAccept= new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    AlertDialog alertDialog = new AlertDialog.Builder(CallActivity.this).create();
                    alertDialog.setTitle("Call Fail");
                    alertDialog.setMessage("The other doesn't want to connect with you");

                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE  , "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            cancelConnect();

                        }
                    });

                    alertDialog.show();

                }
            });

        }
    };

    Emitter.Listener onCreateOffer = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                createOffer = true;
                peerConnection.createOffer(sdpObserver , new MediaConstraints());
                CallActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Received create offer" , Toast.LENGTH_SHORT).show();
                    }
                });


            }
        };



        Emitter.Listener onOffer = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                try {
                    JSONObject obj = (JSONObject) args[0];
                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, obj.getString(SDP));
                    peerConnection.setRemoteDescription(sdpObserver, sdp);
                    peerConnection.createAnswer(sdpObserver, new MediaConstraints());


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                CallActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Received offer" , Toast.LENGTH_SHORT).show();
                    }
                });


            }
        };

        Emitter.Listener onAnswer = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
                            obj.getString(SDP));
                    peerConnection.setRemoteDescription(sdpObserver, sdp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                CallActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Received answer" , Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };

        Emitter.Listener onCandidate = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = (JSONObject) args[0];

                    peerConnection.addIceCandidate(new IceCandidate(obj.getString(SDP_MID),
                            obj.getInt(SDP_M_LINE_INDEX),
                            obj.getString(SDP)));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                CallActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Received candidate" , Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };


    public void cancelConnect(){

        peerConnection.removeStream(localMediaStream);
        peerConnection.close();

        socket.disconnect();
        socket.close();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelConnect();

    }

    SdpObserver sdpObserver = new SdpObserver() {
            @Override
            public void onCreateSuccess(final SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(sdpObserver, sessionDescription);

                try {
                    JSONObject obj = new JSONObject();
                    obj.put(SDP, sessionDescription.description);
                    if (createOffer) {
                        socket.emit(OFFER, obj);
                    } else {
                        socket.emit(ANSWER, obj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSetSuccess() {

            }

            @Override
            public void onCreateFailure(String s) {

            }

            @Override
            public void onSetFailure(String s) {

            }
        };


        PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d("RTCAPP", "onSignalingChange:" + signalingState.toString());
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d("RTCAPP", "onIceConnectionChange:" + iceConnectionState.toString());
                if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED){
                    CallActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(CallActivity.this).create();
                            alertDialog.setTitle("DISCONNECT");
                            alertDialog.setMessage(" Your call has been cancel ! ");

                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE  , "OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    cancelConnect();

                                }
                            });

                            alertDialog.show();

                        }
                    });

                }

            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {

            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put(SDP_MID, iceCandidate.sdpMid);
                    obj.put(SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);
                    obj.put(SDP, iceCandidate.sdp);
                    socket.emit(CANDIDATE, obj);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                mediaStream.videoTracks.getFirst().addRenderer(otherPeerRenderer);

                CallActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext() , "Add stream" , Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {

                cancelConnect();

            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {

            }

            @Override
            public void onRenegotiationNeeded() {

            }
        };






}
