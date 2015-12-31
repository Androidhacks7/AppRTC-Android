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
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.androidhacks7.apprtc_android.utils.JSONConstants;

import org.webrtc.VideoRendererGui;

/**
 * Created by androidhacks7 on 12/24/2015.
 */
public class VideoCallActivity extends Activity implements View.OnClickListener {

    private PeerConnectionManager peerConnectionManager;

    private boolean audioDisabled, videoDisabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        GLSurfaceView videoView = (GLSurfaceView) findViewById(R.id.video_view);
        peerConnectionManager = new PeerConnectionManager(this);
        final String args = getIntent().getStringExtra(JSONConstants.CALL_PARAMS);
        final boolean makeCall = getIntent().getBooleanExtra(JSONConstants.MAKE_CALL, false);
        VideoRendererGui.setView(videoView, new Runnable() {
            @Override
            public void run() {
                peerConnectionManager.createPeerConnectionFactory(VideoCallActivity.this);
                if (makeCall) {
                    peerConnectionManager.makeCall(args);
                } else {
                    peerConnectionManager.acceptRejectCall(true, args);
                }
            }
        });
        if (getIntent().getStringExtra(JSONConstants.REJECT_CALL) != null) {
            peerConnectionManager.acceptRejectCall(false, args);
            finish();
        }
        peerConnectionManager.init();

        ImageButton endCallButton = (ImageButton) findViewById(R.id.end_call);
        ImageButton switchCameraButton = (ImageButton) findViewById(R.id.switch_camera);
        ImageButton muteVideoButton = (ImageButton) findViewById(R.id.mute_video);
        ImageButton muteAudioButton = (ImageButton) findViewById(R.id.mute_audio);

        endCallButton.setOnClickListener(this);
        switchCameraButton.setOnClickListener(this);
        muteVideoButton.setOnClickListener(this);
        muteAudioButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.end_call) {
            peerConnectionManager.disconnect(true);
        } else if (view.getId() == R.id.switch_camera) {
            peerConnectionManager.switchCamera();
        } else if (view.getId() == R.id.mute_audio) {
            if (audioDisabled) {
                peerConnectionManager.updateAudioState(true);
                ((ImageButton) view).setImageResource(R.drawable.ic_un_mute);
                audioDisabled = false;
            } else {
                peerConnectionManager.updateAudioState(false);
                ((ImageButton) view).setImageResource(R.drawable.ic_mute);
                audioDisabled = true;
            }
        } else if (view.getId() == R.id.mute_video) {
            if (videoDisabled) {
                peerConnectionManager.updateVideoState(true);
                ((ImageButton) view).setImageResource(R.drawable.ic_unhide_video);
                videoDisabled = false;
            } else {
                peerConnectionManager.updateVideoState(false);
                ((ImageButton) view).setImageResource(R.drawable.ic_hide_video);
                videoDisabled = true;
            }
        }
    }
}
