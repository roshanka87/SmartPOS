package com.justtide.osapp.util;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String message;
    private String TAG = "firebase_message";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived:" + remoteMessage);
		Intent pushNotification = new Intent("Parameter.FCM_PUSH_NOTIFICATION");
		message  =remoteMessage.getNotification().getBody();
		pushNotification.putExtra("fcmData", message);
		LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
    }
}
