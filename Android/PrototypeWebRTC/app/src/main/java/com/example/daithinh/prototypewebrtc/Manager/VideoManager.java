package com.example.daithinh.prototypewebrtc.Manager;

import android.content.Context;
import android.util.Log;

import com.example.daithinh.prototypewebrtc.Activity.MainActivity;

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
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

public class VideoManager implements PeerConnection.Observer, SdpObserver {

    public static final String SDP                          = "sdp";
    public static final String SDP_M_LINE_INDEX             = "sdpMLineIndex";
    public static final String SDP_MID                      = "sdpMid";


    public interface SignalingObserver {
        void onSdpObserveCreateSuccess(SessionDescription sessionDescription);
        void onIceCandidate(IceCandidate iceCandidate);
        void onAddStream();
    }

    private SignalingObserver observer;

    public void registerObserver(SignalingObserver signalingObserver){
        observer = signalingObserver;
    }

    public void removeObserver(){
        observer = null;
    }

    private void notifySdpObserveCreateSuccess(SessionDescription description){
        observer.onSdpObserveCreateSuccess(description);
    }

    private static VideoManager mInstance;
    private Context mContext;

    // Peer Connection and Peer Connection Factory
    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;

    // Audio + Video source
    private AudioSource audioSource;
    private VideoSource localVideoSource;

    // Audio + Video track
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;

    // Media stream
    private MediaStream localMediaStream;


    private static final String VIDEO_TRACK_ID = "video1";
    private static final String AUDIO_TRACK_ID = "audio1";
    private static final String LOCAL_STREAM_ID = "stream1";


    public void renderVideoTrack(VideoRenderer renderer){
        localVideoTrack.addRenderer(renderer);
    }


    public static VideoManager getInstance(){
        return getInstance(MainActivity.getInstance());
    }

    public static VideoManager getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new VideoManager(context);
        }

        return mInstance;
    }

    public VideoManager(Context context){
        mContext = context;

        initialize();
    }

    public void initialize(){

//        // Set speaker on
//        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        audioManager.setSpeakerphoneOn(true);

        PeerConnectionFactory.initializeAndroidGlobals(
                mContext,  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true,  // Hardware Acceleration Enabled
                null); // Render EGL Context

        peerConnectionFactory = new PeerConnectionFactory();

        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("turn:numb.viagenie.ca" , "ndthinh.tma@gmail.com" , "ndthinhwebrtc"));


        peerConnection = peerConnectionFactory.createPeerConnection(
                iceServers,
                new MediaConstraints(),
                this);

        // init Local Audio track
        audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);

        // init Local Video track
        VideoCapturerAndroid vc = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfFrontFacingDevice(), null);
        localVideoSource = peerConnectionFactory.createVideoSource(vc, new MediaConstraints());
        localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
        localVideoTrack.setEnabled(true);

        localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID);

        addStream();
    }

    public void addStream(){
        localMediaStream.addTrack(localAudioTrack);
        localMediaStream.addTrack(localVideoTrack);

        peerConnection.addStream(localMediaStream);
    }


    private MediaStream remoteMediaStream = null;

    public MediaStream getRemoteMediaStream(){
        return  remoteMediaStream;
    }

    /* -------------- Peer Connection Observe -------------- */

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d("TAG", "VideoManager.onSignalingChange(): ");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d("TAG", "VideoManager.onIceConnectionChange(): ");
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d("TAG", "VideoManager.onIceConnectionReceivingChange(): ");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d("TAG", "VideoManager.onIceGatheringChange(): ");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

        Log.d("TAG", "VideoManager.onIceCandidate(): ");

        observer.onIceCandidate(iceCandidate);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d("TAG", "VideoManager.onAddStream(): ");

        remoteMediaStream = mediaStream;

        observer.onAddStream();
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d("TAG", "VideoManager.onRemoveStream(): ");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d("TAG", "VideoManager.onDataChannel(): ");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d("TAG", "VideoManager.onRenegotiationNeeded(): ");
    }
    /* -------------- Peer Connection Observe -------------- */




    public void makeOffer(){
        peerConnection.createOffer(this, new MediaConstraints());
    }

    public void makeAnswer(){
        peerConnection.createAnswer(this, new MediaConstraints());
    }

    public void setRemoteDescription(SessionDescription remoteDescription){
        Log.d("TAG", "VideoManager.setRemoteDescription(): ");

        peerConnection.setRemoteDescription(this, remoteDescription);
    }

    public void addIceCandidate(IceCandidate iceCandidate){
        peerConnection.addIceCandidate(iceCandidate);
    }





    /* -------------- Sdp Observe -------------- */
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d("TAG", "VideoManager.SdpObserve.onCreateSuccess(): ");

        peerConnection.setLocalDescription(this, sessionDescription);
        notifySdpObserveCreateSuccess(sessionDescription);
    }

    @Override
    public void onSetSuccess() {
        Log.d("TAG", "VideoManager.SdpObserve.onSetSuccess(): ");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d("TAG", "VideoManager.SdpObserve.onCreateFailure(): ");
    }

    @Override
    public void onSetFailure(String s) {
        Log.d("TAG", "VideoManager.SdpObserve.onSetFailure(): ");
    }
    /* -------------- Sdp Observe -------------- */
}
