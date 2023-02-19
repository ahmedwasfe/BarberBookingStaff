package com.ahmet.barberbookingstaff.service;

import android.util.Log;

import com.ahmet.barberbookingstaff.common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Common.updateToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> dataRecv = remoteMessage.getData();

        if (dataRecv != null) {

            Common.showNotification(this, new Random().nextInt(),
                    dataRecv.get(Common.KEY_NOTFI_TITLE),
                    dataRecv.get(Common.KEY_NOTFI_CONTENT),
                    null);
        }
    }
}
