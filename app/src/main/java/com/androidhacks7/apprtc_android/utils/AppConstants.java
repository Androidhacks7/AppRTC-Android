/*
 * Copyright (c) 2016 Androidhacks7
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.androidhacks7.apprtc_android.utils;

/**
 * Created by androidhacks7 on 12/24/2015.
 */
public class AppConstants {

    // Outgoing signal messages

    public static final int DIALOG_USERNAME = 0;

    public static final int DIALOG_ACCEPT_REJECT = 1;


    public static final String MAKE_CALL = "call";

    public static final String ACCEPT_REJECT_CALL = "call_status";

    public static final String SEND_SDP = "on_sdp";

    public static final String SEND_ICE_CANDIDATE = "on_ice_candidate";

    // Incoming messages

    public static final String RECEIVER_ACCEPTED_CALL = "receiver_accepted_call";

    public static final String RECEIVER_REJECTED_CALL = "receiver_rejected_call";

    public static final String REMOTE_SDP_RECEIVED = "remote_sdp_received";

    public static final String ICE_CANDIDATE_RECEIVED = "ice_candidate_received";


    public static final String INCOMING_CALL = "incomingcall";

    public static final String DISCONNECT = "on_disconnect";

}
