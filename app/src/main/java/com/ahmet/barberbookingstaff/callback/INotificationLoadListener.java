package com.ahmet.barberbookingstaff.callback;

import com.ahmet.barberbookingstaff.model.Notifications.GetNotification;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface INotificationLoadListener {

    void onLoadNotificationSuccess(List<GetNotification> mListGetNotifications, DocumentSnapshot lastDocument);
    void inLoadNotificationFailed(String error);
}
