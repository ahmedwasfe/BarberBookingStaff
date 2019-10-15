package com.ahmet.barberbookingstaff.Service;

import com.ahmet.barberbookingstaff.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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

////        Toast.makeText(this, "Show Notifiaction", Toast.LENGTH_SHORT).show();
//        Common.testService(this);

       // Intent intent = new Intent(this, DoneServicsesActivity.class);
        Common.showNotification(this,
                new Random().nextInt(),
                "New Booking",
                "You have a new booking",
                null);
    }
}
