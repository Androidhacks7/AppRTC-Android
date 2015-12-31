/*
 * Copyright (c) 2016 Androidhacks7
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.androidhacks7.apprtc_android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.androidhacks7.apprtc_android.model.SignalingParameters;
import com.androidhacks7.apprtc_android.utils.AppConstants;
import com.androidhacks7.apprtc_android.utils.JSONConstants;
import com.androidhacks7.apprtc_android.listeners.SocketMessageListener;

import org.appspot.apprtc.PeerConnectionClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;

/**
 * Created by androidhacks7 on 12/24/2015.
 */
public class PeerConnectionManager implements PeerConnectionClient.PeerConnectionEvents {

    private static final String TAG = PeerConnectionManager.class.getSimpleName();

    private Context context;
    private VideoRenderer.Callbacks localRender, remoteRender;

    private PeerConnectionClient peerConnectionClient;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;

    private String caller, receiver;
    private boolean iceConnected;

    private SocketMessageListener socketMessageListener = new SocketMessageHandler();

    public PeerConnectionManager(Context context) {
        this.context = context;
    }

    public void init() {
        remoteRender = VideoRendererGui.create(
                0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, false);
        localRender = VideoRendererGui.create(
                0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);

        peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
                true,
                false,
                0, 0, 0, 0, "VP9", true, 0, "OPUS", true, false);

        SocketManager.getInstance().setReceiver(socketMessageListener);
    }

    public void createPeerConnectionFactory(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {

                    Log.i(TAG, "Creating peer connection factory");
                    peerConnectionClient = PeerConnectionClient.getInstance();
                    peerConnectionClient.createPeerConnectionFactory(context, peerConnectionParameters,
                            PeerConnectionManager.this);
                }
            }
        });
    }

    private void updateVideoView() {
        // Alter remote/local video width/height as desired
        VideoRendererGui.update(remoteRender,
                0, 0,
                100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, false);
        if (iceConnected) {
            VideoRendererGui.update(localRender,
                    72, 72,
                    25, 25,
                    RendererCommon.ScalingType.SCALE_ASPECT_FIT, true);
        } else {
            VideoRendererGui.update(localRender,
                    0, 0,
                    100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);
        }
    }

    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject sdpObject = new JSONObject();
                try {
                    sdpObject.put(JSONConstants.SDP_TYPE, sdp.type);
                    sdpObject.put(JSONConstants.SDP, sdp.description);
                    if (sdp.type == SessionDescription.Type.OFFER) {
                        sdpObject.put(JSONConstants.CALLER, caller);
                        sdpObject.put(JSONConstants.RECEIVER, receiver);
                    } else {
                        sdpObject.put(JSONConstants.CALLER, receiver);
                        sdpObject.put(JSONConstants.RECEIVER, caller);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SocketManager.getInstance().onSend(AppConstants.SEND_SDP, sdpObject);
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject iceObject = new JSONObject();
                try {
                    iceObject.put(JSONConstants.SDP_MID, candidate.sdpMid);
                    iceObject.put(JSONConstants.CANDIDATE, candidate.sdp.replace(JSONConstants.CANDIDATE, "a=candidate"));
                    iceObject.put(JSONConstants.SDP_MID_LINE_INDEX, candidate.sdpMLineIndex);
                    iceObject.put(JSONConstants.CALLER, caller);
                    iceObject.put(JSONConstants.RECEIVER, receiver);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SocketManager.getInstance().onSend(AppConstants.SEND_ICE_CANDIDATE, iceObject);
            }
        });
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "ICE connected");
                iceConnected = true;
                updateVideoView();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "ICE disconnected");
                iceConnected = false;
                disconnect(false);
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
    }

    @Override
    public void onPeerConnectionError(String description) {
        Log.e(TAG, "onPeerConnectionError: " + description);
    }

    public void onConnectedToRoom(final boolean initiator) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.w(TAG, "Room is connected, but EGL context is not ready yet.");
                    return;
                }
                PeerConnection.IceServer stunServer = new PeerConnection.IceServer("stun:stun.l.google.com:19302", "", "");
                ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
                iceServers.add(stunServer);
                Log.i(TAG, "Creating peer connection");
                peerConnectionClient.createPeerConnection(VideoRendererGui.getEGLContext(),
                        localRender, remoteRender, new SignalingParameters(iceServers, initiator));

                if (initiator) {
                    Log.i(TAG, "Creating OFFER...");
                    // Create offer. Offer SDP will be sent to answering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createOffer();
                }
            }
        });
    }

    public void onRemoteDescription(final SessionDescription sdp, final boolean initiator) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                Log.i(TAG, "Received remote " + sdp.type);
                peerConnectionClient.setRemoteDescription(sdp);
                if (initiator) {
                    Log.i(TAG, "Creating ANSWER...");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG,
                            "Received ICE candidate for non-initilized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    public void disconnect(boolean initiatedDisconnect) {
        if (initiatedDisconnect) {
            JSONObject disconnect = new JSONObject();
            try {
                disconnect.put(JSONConstants.INITIATOR, caller);
                disconnect.put(JSONConstants.PEER, receiver);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SocketManager.getInstance().onSend(AppConstants.DISCONNECT, disconnect);
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        ((Activity) context).finish();
    }

    public void switchCamera() {
        peerConnectionClient.switchCamera();
    }

    public void updateVideoState(boolean flag) {
        peerConnectionClient.setVideoEnabled(flag);
    }

    public void updateAudioState(boolean flag) {
        peerConnectionClient.setAudioEnabled(flag);
    }

    private void runOnUiThread(Runnable runnable) {
        ((Activity) context).runOnUiThread(runnable);
    }

    private class SocketMessageHandler implements SocketMessageListener {

        public void onMessage(SocketMessage message, Object... args) {
            try {
                switch (message) {
                    case ACCEPT:
                        Log.d(TAG, "Receiver accepted call");
                        onConnectedToRoom(true);
                        break;
                    case REJECT:
                        Log.d(TAG, "Receiver rejected call");
                        ((Activity) context).finish();
                        break;
                    case SDP:
                        JSONObject sdpObject = new JSONObject(args[0].toString());
                        String type = sdpObject.getString(JSONConstants.SDP_TYPE);
                        String sdp = sdpObject.getString(JSONConstants.SDP);
                        SessionDescription sessionDescription = null;
                        if (type.equalsIgnoreCase(JSONConstants.OFFER)) {
                            sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
                        } else if (type.equalsIgnoreCase(JSONConstants.ANSWER)) {
                            sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                        }
                        Log.d(TAG, "Remote SDP received " + sessionDescription);
                        onRemoteDescription(sessionDescription, false);
                        break;
                    case ICE:
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        IceCandidate iceCandidate = new IceCandidate(jsonObject.getString(JSONConstants.SDP_MID), jsonObject.getInt(JSONConstants.SDP_MID_LINE_INDEX), jsonObject.getString(JSONConstants.CANDIDATE));
                        Log.d(TAG, "ICE Candidate received " + iceCandidate);
                        onRemoteIceCandidate(iceCandidate);
                        break;
                    case DISCONNECT:
                        Log.d(TAG, "Disconnect initiated");
                        disconnect(false);
                        break;
                    default:
                        throw new RuntimeException("Wrong signal message received");
                }
            } catch (JSONException jsonException) {
                Log.e(TAG, jsonException.toString());
            }
        }

    }

    public void makeCall(Object... args) {
        try {
            JSONObject jsonObject = new JSONObject(args[0].toString());
            this.caller = (String) jsonObject.get(JSONConstants.CALLER);
            this.receiver = (String) jsonObject.get(JSONConstants.RECEIVER);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject callObject = new JSONObject();
        try {
            callObject.put(JSONConstants.CALLER, caller);
            callObject.put(JSONConstants.RECEIVER, receiver);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SocketManager.getInstance().onSend(AppConstants.MAKE_CALL, callObject);
    }

    public void acceptRejectCall(boolean status, Object... args) {
        JSONObject objectToSend = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(args[0].toString());
            if (status) {
                this.caller = (String) jsonObject.get(JSONConstants.CALLER);
                this.receiver = (String) jsonObject.get(JSONConstants.RECEIVER);
                onConnectedToRoom(false);
            }
            objectToSend.put(JSONConstants.STATUS, status);
            objectToSend.put(JSONConstants.CALLER, jsonObject.get(JSONConstants.CALLER));
            objectToSend.put(JSONConstants.RECEIVER, jsonObject.get(JSONConstants.RECEIVER));
        } catch (Exception e) {

        }
        SocketManager.getInstance().onSend(AppConstants.ACCEPT_REJECT_CALL, objectToSend);
    }
}