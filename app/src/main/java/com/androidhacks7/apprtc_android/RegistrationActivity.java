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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.androidhacks7.apprtc_android.utils.AppConstants;
import com.androidhacks7.apprtc_android.utils.JSONConstants;
import com.androidhacks7.apprtc_android.utils.ServerConfiguration;
import com.androidhacks7.apprtc_android.listeners.CallListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by androidhacks7 on 12/24/2015.
 */
public class RegistrationActivity extends Activity implements CallListener {

    private EditText userName;

    private static final String TAG = RegistrationActivity.class.getSimpleName();

    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        SocketManager socketManager = SocketManager.getInstance();
        socketManager.setReceiver(this);
        socketManager.init();
        showUserDialog(AppConstants.DIALOG_USERNAME, null);
    }

    private void showUserDialog(int type, final Object... args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (type == AppConstants.DIALOG_USERNAME) {
            builder.setMessage("Enter User Name: ");
            userName = new EditText(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            userName.setLayoutParams(lp);
            builder.setView(userName);
            builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    register();
                }
            });
            builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        } else if (type == 1) {
            try {
                final JSONObject jsonObject = new JSONObject(args[0].toString());
                builder.setMessage("Incoming call from " + jsonObject.get(JSONConstants.CALLER));
                builder.setPositiveButton("Attend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(RegistrationActivity.this, VideoCallActivity.class);
                        intent.putExtra(JSONConstants.CALL_PARAMS, args[0].toString());
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(RegistrationActivity.this, VideoCallActivity.class);
                        intent.putExtra(JSONConstants.CALL_PARAMS, args[0].toString());
                        intent.putExtra(JSONConstants.REJECT_CALL, args[0].toString());
                        startActivity(intent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        builder.create().show();
    }

    private void register() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject userParams = new JSONObject();
        try {
            currentUser = userName.getText().toString();
            userParams.put(JSONConstants.USER_NAME, currentUser);
            userParams.put(JSONConstants.DEVICE_ID, Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            userParams.put(JSONConstants.SOCKET_ID, SocketManager.getInstance().getSocketId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ServerConfiguration.REGISTRATION_URL, userParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "User registration successful");
                parseUserList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "User registration error " + error.getMessage());
            }
        });
        queue.add(request);
    }

    private void parseUserList(JSONObject jsonObject) {
        ArrayList<String> userNames = new ArrayList<String>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(JSONConstants.USERS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject user = (JSONObject) jsonArray.get(i);
                userNames.add((String) user.get(JSONConstants.USER_NAME));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateUI(userNames);
    }

    private void updateUI(final ArrayList<String> userNames) {
        ListView listView = (ListView) findViewById(R.id.userList);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userNames));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Initiating call to " + userNames.get(i));
                Intent intent = new Intent(RegistrationActivity.this, VideoCallActivity.class);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(JSONConstants.CALLER, currentUser);
                    jsonObject.put(JSONConstants.RECEIVER, userNames.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra(JSONConstants.MAKE_CALL, true);
                intent.putExtra(JSONConstants.CALL_PARAMS, jsonObject.toString());
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onCallReceived(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showUserDialog(AppConstants.DIALOG_ACCEPT_REJECT, args);
            }
        });
    }
}
