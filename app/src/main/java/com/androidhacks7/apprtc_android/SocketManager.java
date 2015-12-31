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

import com.androidhacks7.apprtc_android.utils.AppConstants;
import com.androidhacks7.apprtc_android.utils.ServerConfiguration;
import com.androidhacks7.apprtc_android.listeners.CallListener;
import com.androidhacks7.apprtc_android.listeners.Receiver;
import com.androidhacks7.apprtc_android.listeners.SocketMessageListener;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by androidhacks7 on 12/19/2015.
 */
public class SocketManager {

    public static final String TAG = SocketManager.class.getSimpleName();

    private static SocketManager instance = null;

    private Socket mSocket;

    private SocketMessageListener socketMessageListener;
    private CallListener callListener;

    private SocketManager() {
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void init() {
        try {
            mSocket = IO.socket(ServerConfiguration.SOCKET_ENDPOINT);
        } catch (URISyntaxException e) {
        }
        mSocket.connect();
        initCallbacks();
    }

    public void setReceiver(Receiver receiver) {
        if (receiver instanceof CallListener) {
            this.callListener = (CallListener) receiver;
        } else {
            this.socketMessageListener = (SocketMessageListener) receiver;
        }
    }

    public String getSocketId() {
        return mSocket.id();
    }

    private void initCallbacks() {
        mSocket.on(AppConstants.INCOMING_CALL, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                callListener.onCallReceived(args);
            }
        });

        mSocket.on(AppConstants.RECEIVER_ACCEPTED_CALL, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketMessageListener.onMessage(SocketMessageListener.SocketMessage.ACCEPT, args);
            }
        });

        mSocket.on(AppConstants.RECEIVER_REJECTED_CALL, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketMessageListener.onMessage(SocketMessageListener.SocketMessage.REJECT, args);
            }
        });

        mSocket.on(AppConstants.REMOTE_SDP_RECEIVED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketMessageListener.onMessage(SocketMessageListener.SocketMessage.SDP, args);
            }
        });

        mSocket.on(AppConstants.ICE_CANDIDATE_RECEIVED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketMessageListener.onMessage(SocketMessageListener.SocketMessage.ICE, args);
            }
        });

        mSocket.on(AppConstants.DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketMessageListener.onMessage(SocketMessageListener.SocketMessage.DISCONNECT, args);
            }
        });
    }

    public void onSend(String message, Object... args) {
        mSocket.emit(message, args);
    }
}
