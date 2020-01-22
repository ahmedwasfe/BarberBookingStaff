package com.ahmet.barberbookingstaff.Service;

import android.app.Notification;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.SubActivity.DoneServicsesActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.i("Token_SERVICE", token);

        Common.updateToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

////        Toast.makeText(this, "Show Notifiaction", Toast.LENGTH_SHORT).show();
//        Common.testService(this);
//
//        Intent intent = new Intent(this, DoneServicsesActivity.class);
//        startActivity(intent);

        Common.showNotification(this,
                new Random().nextInt(),
                "New Booking",
                "You have a new booking from ",
                null);
    }
}
