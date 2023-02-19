package com.ahmet.barberbookingstaff.ui.notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.adapter.NotificationAdapter;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.callback.INotificationLoadListener;
import com.ahmet.barberbookingstaff.model.Notifications.GetNotification;
import com.ahmet.barberbookingstaff.model.Notifications.SetNotification;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
    @BindView(R.id.linear_no_notifications)
    LinearLayout mLinearNoNotification;
    @BindView(R.id.text_no_notifications)
    TextView mTxtNoNotification;

    private CollectionReference mNotificationCollection;
    private DocumentSnapshot finalDocumentLocal;

    private INotificationLoadListener iNotificationLoadListener;

    private int totalItem = 0, lastVisibleItem;
    private boolean isLoading = false, isMaxData = false;

    private NotificationAdapter mNotificationAdapter;
    private List<GetNotification> mFirstListGetNotification = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificatins);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.notifications);

        ButterKnife.bind(this);

        mLinearNoNotification.setVisibility(View.GONE);

        init();
        initView();

        loadNotifications(null);

    }

    private void setNotificationsRead(){

        FirebaseFirestore.getInstance()
                .collection(Common.KEY_AllSALON_REFERANCE)
                .document(Common.currentSalon.getSalonId())
                .collection(Common.KEY_BARBER_REFERANCE)
                .document(Common.currentBarber.getBarberId())
                .collection(Common.KEY_NOTIFICATIONS_REFERANCE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        SetNotification setNotification = new SetNotification();

                        for (DocumentSnapshot snapshot : task.getResult()){
                            boolean isRead = snapshot.getBoolean("read");
                            if (isRead) {
                                setNotification.setRead(true);
                                FirebaseFirestore.getInstance()
                                        .collection(Common.KEY_AllSALON_REFERANCE)
                                        .document(Common.currentSalon.getSalonId())
                                        .collection(Common.KEY_BARBER_REFERANCE)
                                        .document(Common.currentBarber.getBarberId())
                                        .collection(Common.KEY_NOTIFICATIONS_REFERANCE)
                                        .add(setNotification)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                // if (task.isSuccessful())

                                            }
                                        });

                            }

                        }
                    }
                });
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
                .collection(Common.KEY_AllSALON_REFERANCE)
                .document(Common.currentSalon.getSalonId())
                .collection(Common.KEY_BARBER_REFERANCE)
                .document(Common.currentBarber.getBarberId())
                .collection(Common.KEY_NOTIFICATIONS_REFERANCE);

        if (lastDocument == null){

            mNotificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                    .limit(Common.MAX_NOTIFICATIONS_PER_LOAD)
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()){

                            List<GetNotification> mLastListNotificaton = new ArrayList<>();

                            DocumentSnapshot finalDocument = null;

                            for (DocumentSnapshot documentSnapshot : task.getResult()){

                                GetNotification getNotification = documentSnapshot.toObject(GetNotification.class);
                                mLastListNotificaton.add(getNotification);
                                finalDocument = documentSnapshot;
                            }

                            iNotificationLoadListener.onLoadNotificationSuccess(mLastListNotificaton, finalDocument);

                        }
                    })
                    .addOnFailureListener(e -> iNotificationLoadListener.inLoadNotificationFailed(e.getMessage()));

        } else {

            if (!isMaxData){

                mNotificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDocument)
                        .limit(Common.MAX_NOTIFICATIONS_PER_LOAD)
                        .get()
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()){

                                List<GetNotification> mLastListNotificaton = new ArrayList<>();

                                DocumentSnapshot finalDocument = null;

                                for (DocumentSnapshot documentSnapshot : task.getResult()){

                                    GetNotification getNotification = documentSnapshot.toObject(GetNotification.class);
                                    mLastListNotificaton.add(getNotification);
                                    finalDocument = documentSnapshot;
                                }

                                iNotificationLoadListener.onLoadNotificationSuccess(mLastListNotificaton, finalDocument);

                            }
                        })
                        .addOnFailureListener(e -> iNotificationLoadListener.inLoadNotificationFailed(e.getMessage()));
            }
        }

    }

    private void init() {

        iNotificationLoadListener = this;
    }

    @Override
    public void onLoadNotificationSuccess(List<GetNotification> mListGetNotifications, DocumentSnapshot lastDocument) {

        if (mListGetNotifications.size() == 0 && mFirstListGetNotification.size() == 0){
            mLinearNoNotification.setVisibility(View.VISIBLE);
            mTxtNoNotification.setText(getString(R.string.no_notifications));
        }

        if (lastDocument != null){

            if (lastDocument.equals(finalDocumentLocal))
                isMaxData = true;
            else {
                finalDocumentLocal = lastDocument;
                isMaxData = false;
            }

            if (mNotificationAdapter == null && mFirstListGetNotification.size() == 0){

                mNotificationAdapter = new NotificationAdapter(this, mListGetNotifications);
                mFirstListGetNotification = mListGetNotifications;

            } else {

                if (!mListGetNotifications.equals(mFirstListGetNotification))
                    mNotificationAdapter.updateList(mListGetNotifications);
            }

            mRecyclerNotifications.setAdapter(mNotificationAdapter);
        }

    }

    @Override
    public void inLoadNotificationFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}