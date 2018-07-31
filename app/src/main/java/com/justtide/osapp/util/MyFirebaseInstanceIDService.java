package com.justtide.osapp.util;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("fcm_token","token:"+refreshedToken);

        Intent registrationComplete = new Intent("Parameter.FCM_TOKEN_RECEIVER");
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        Log.d("send_data", "sendRegistrationToServer: " + token);
    }

}
