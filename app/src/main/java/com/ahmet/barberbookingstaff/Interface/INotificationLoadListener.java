package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.Notification;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface INotificationLoadListener {

    void onLoadNotificationSuccess(List<Notification> mListNotifications , DocumentSnapshot lastDocument);
    void inLoadNotificationFailed(String error);
}
