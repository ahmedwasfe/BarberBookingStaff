package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.NotificationAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Interface.INotificationLoadListener;
import com.ahmet.barberbookingstaff.Model.Notification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsActivity extends AppCompatActivity implements INotificationLoadListener {

    @BindView(R.id.recycler_notifications)
    RecyclerView mRecyclerNotifications;

    private CollectionReference mNotificationCollection;
    private DocumentSnapshot finalDocumentLocal;

    private INotificationLoadListener iNotificationLoadListener;

    private int totalItem = 0, lastVisibleItem;
    private boolean isLoading = false, isMaxData = false;

    private NotificationAdapter mNotificationAdapter;
    private List<Notification> mFirstListNotification = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificatins);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");

        ButterKnife.bind(this);

        init();
        initView();

        loadNotifications(null);
    }

    private void initView() {

        mRecyclerNotifications.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerNotifications.setLayoutManager(layoutManager);
        mRecyclerNotifications.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        mRecyclerNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItem = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItem <= (lastVisibleItem + Common.MAX_NOTIFICATIONS_PER_LOAD)){
                     loadNotifications(finalDocumentLocal);
                     isLoading = true;
                }
            }
        });
    }

    private void loadNotifications(DocumentSnapshot lastDocument) {

        // /AllSalon/Gaza/Branch/AFXjgtlJwztf7cLFumNT/Barber/utQmhc07WVjaZdr9tbRB/Notifications
        mNotificationCollection = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.cityName)
                .collection("Branch")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberID())
                .collection("Notifications");

        if (lastDocument == null){

            mNotificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                    .limit(Common.MAX_NOTIFICATIONS_PER_LOAD)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()){

                                List<Notification> mLastListNotificaton = new ArrayList<>();

                                DocumentSnapshot finalDocument = null;

                                for (DocumentSnapshot documentSnapshot : task.getResult()){

                                    Notification notification = documentSnapshot.toObject(Notification.class);
                                    mLastListNotificaton.add(notification);
                                    finalDocument = documentSnapshot;
                                }

                                iNotificationLoadListener.onLoadNotificationSuccess(mLastListNotificaton, finalDocument);

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    iNotificationLoadListener.inLoadNotificationFailed(e.getMessage());
                }
            });

        } else {

            if (!isMaxData){

                mNotificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDocument)
                        .limit(Common.MAX_NOTIFICATIONS_PER_LOAD)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()){

                                    List<Notification> mLastListNotificaton = new ArrayList<>();

                                    DocumentSnapshot finalDocument = null;

                                    for (DocumentSnapshot documentSnapshot : task.getResult()){

                                        Notification notification = documentSnapshot.toObject(Notification.class);
                                        mLastListNotificaton.add(notification);
                                        finalDocument = documentSnapshot;
                                    }

                                    iNotificationLoadListener.onLoadNotificationSuccess(mLastListNotificaton, finalDocument);

                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iNotificationLoadListener.inLoadNotificationFailed(e.getMessage());
                            }
                        });
            }
        }

    }

    private void init() {

        iNotificationLoadListener = this;
    }

    @Override
    public void onLoadNotificationSuccess(List<Notification> mListNotifications, DocumentSnapshot lastDocument) {

        if (lastDocument != null){

            if (lastDocument.equals(finalDocumentLocal))
                isMaxData = true;
            else {
                finalDocumentLocal = lastDocument;
                isMaxData = false;
            }

            if (mNotificationAdapter == null && mFirstListNotification.size() == 0){

                mNotificationAdapter = new NotificationAdapter(this, mListNotifications);
                mFirstListNotification = mListNotifications;

            } else {

                if (!mListNotifications.equals(mFirstListNotification))
                    mNotificationAdapter.updateList(mListNotifications);
            }

            mRecyclerNotifications.setAdapter(mNotificationAdapter);
        }

    }

    @Override
    public void inLoadNotificationFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}
