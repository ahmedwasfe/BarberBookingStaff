package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ahmet.barberbookingstaff.Adapter.TimeSlotAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.Interface.ITimeSlotLoadListener;
import com.ahmet.barberbookingstaff.Model.BookingInformation;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.SubActivity.NotificationsActivity;
import com.ahmet.barberbookingstaff.SubActivity.SettingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

import static com.ahmet.barberbookingstaff.Common.Common.mSimpleDateFormat;
import static com.ahmet.barberbookingstaff.Common.Common.setFragment;

public class HomeFragment extends Fragment implements ITimeSlotLoadListener {

    private Unbinder mUnbinder;

    @BindView(R.id.recycler_time_solt)
    RecyclerView mRecyclerTimeSolt;
    @BindView(R.id.calendar_time_slot)
    HorizontalCalendarView mCalendarDateView;


    private DocumentReference mDocReferenceBarber;
    private CollectionReference currentBookingDateCollectionRef;

    private EventListener<QuerySnapshot> bookingEventListener;
    private ListenerRegistration bookingRealTimeListener;

    // init interface
    private ITimeSlotLoadListener iTimeSlotLoadListener;

    private AlertDialog mDialog;

    private static HomeFragment instance;
    public static HomeFragment getInstance(){

        if (instance == null)
            instance = new HomeFragment();

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_home, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();
        initView(layoutView);

        return layoutView;
    }

    private void init() {

        iTimeSlotLoadListener = this;

        initBookingRealTimeUpdate();

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setCancelable(false)
                .setMessage(R.string.please_wait)
                .build();
    }

    private void initBookingRealTimeUpdate() {

        // /AllSalon/Gaza/Branch/AFXjgtlJwztf7cLFumNT/Barber/utQmhc07WVjaZdr9tbRB
        mDocReferenceBarber = FirebaseFirestore.getInstance()
                .collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .collection(Common.KEY_COLLECTION_BARBER)
                .document(Common.currentBarber.getBarberID());

        // get current Date
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DATE, 0);
        bookingEventListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                // If have any new booking, update adapter
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                        mSimpleDateFormat.format(currentDate.getTime()));
            }
        };

        currentBookingDateCollectionRef = mDocReferenceBarber
                .collection(mSimpleDateFormat.format(currentDate.getTime()));
        bookingRealTimeListener = currentBookingDateCollectionRef.addSnapshotListener(bookingEventListener);

    }

    private void initView(View layoutView) {

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);  // Add current date

        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                Common.mSimpleDateFormat.format(date.getTime()));

        // Recycler View
        mRecyclerTimeSolt.setHasFixedSize(true);
        mRecyclerTimeSolt.setLayoutManager(new StaggeredGridLayoutManager(3, LinearLayout.VERTICAL));
        mRecyclerTimeSolt.addItemDecoration(new SpacesItemDecoration(8));

        // HorizontalCalendarView
        mCalendarDateView = layoutView.findViewById(R.id.calendar_time_slot);

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);  // 2 day left

        HorizontalCalendar mCalendarDate = new HorizontalCalendar.Builder(layoutView, R.id.calendar_time_slot)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();

        mCalendarDate.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {

                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;  // this code will not load again you selecte day same with day selected
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                            mSimpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    private void loadAvailableTimeSlotOfBarber(String barberID, String bookingDate) {

        mDialog.show();


        // Get informatio for this barber
        mDocReferenceBarber.get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) { // If babrber available

                            // Get information of booking
                            // If not created return empty
                            CollectionReference mReferenceDate = FirebaseFirestore.getInstance()
                                    .collection(Common.KEY_COLLECTION_AllSALON)
                                    .document(Common.currentSalon.getSalonID())
                                    .collection(Common.KEY_COLLECTION_BARBER)
                                    .document(barberID)
                                    .collection(bookingDate);  // book date is date simpleformat with dd_MM_yyyy == 27_06_2019

                            mReferenceDate.get()
                                    .addOnCompleteListener(task1 -> {

                                        if (task1.isSuccessful()) {

                                            QuerySnapshot querySnapshot = task1.getResult();
                                            if (querySnapshot.isEmpty()) {  // If do not have any appoment

                                                iTimeSlotLoadListener.onTimeSoltLoadEmpty();
                                            } else {
                                                // If have appoiment
                                                List<BookingInformation> mListTimeSlot = new ArrayList<>();
                                                for (QueryDocumentSnapshot snapshot : task1.getResult())
                                                    mListTimeSlot.add(snapshot.toObject(BookingInformation.class));

                                                iTimeSlotLoadListener.onTimeSoltLoadSuccess(mListTimeSlot);
                                            }
                                        }
                                    }).addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSoltLoadFailed(e.getMessage()));
                        }
                    }
                });

    }

    @Override
    public void onTimeSoltLoadSuccess(List<BookingInformation> mListTimeSlot) {

        TimeSlotAdapter mTimeSlotAdapter = new TimeSlotAdapter(getActivity(), mListTimeSlot);
        mRecyclerTimeSolt.setAdapter(mTimeSlotAdapter);

        mDialog.dismiss();
    }

    @Override
    public void onTimeSoltLoadFailed(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeSoltLoadEmpty() {

        TimeSlotAdapter mTimeSlotAdapter = new TimeSlotAdapter(getActivity());
        mRecyclerTimeSolt.setAdapter(mTimeSlotAdapter);

        mDialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        initBookingRealTimeUpdate();
    }

    @Override
    public void onStop() {

        if (bookingRealTimeListener != null)
            bookingRealTimeListener.remove();

        super.onStop();
    }

    @Override
    public void onDestroy() {

        if (bookingRealTimeListener != null)
            bookingRealTimeListener.remove();


        super.onDestroy();
    }

}
