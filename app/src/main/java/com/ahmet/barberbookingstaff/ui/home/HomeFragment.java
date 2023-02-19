package com.ahmet.barberbookingstaff.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ahmet.barberbookingstaff.adapter.TimeSlotAdapter;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.callback.ITimeSlotLoadListener;
import com.ahmet.barberbookingstaff.model.BookingInformation;
import com.ahmet.barberbookingstaff.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

import static com.ahmet.barberbookingstaff.common.Common.mSimpleDateFormat;

public class HomeFragment extends Fragment implements ITimeSlotLoadListener {

    private Unbinder mUnbinder;

    @BindView(R.id.recycler_time_solt)
    RecyclerView mRecyclerTimeSolt;
    @BindView(R.id.calendar_time_slot)
    HorizontalCalendarView mCalendarDateView;


    private DatabaseReference mReferenceBarber;
    private DatabaseReference currentBookingDateRef;


    // init interface
    private ITimeSlotLoadListener iTimeSlotLoadListener;

    private AlertDialog mDialog;

    private static HomeFragment instance;

    public static HomeFragment getInstance() {

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
        mReferenceBarber = FirebaseDatabase.getInstance()
                .getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId());

        // get current Date
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DATE, 0);
        // If have any new booking, update adapter
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                mSimpleDateFormat.format(currentDate.getTime()));

        currentBookingDateRef = mReferenceBarber
                .child(mSimpleDateFormat.format(currentDate.getTime()));

    }

    private void initView(View layoutView) {

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);  // Add current date

        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
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
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            mSimpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    private void loadAvailableTimeSlotOfBarber(String barberID, String bookingDate) {

        // Get informatio for this barber
        mReferenceBarber.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) { // If babrber available

                    // Get information of booking
                    // If not created return empty
                    DatabaseReference mReferenceDate = FirebaseDatabase.getInstance()
                            .getReference()
                            .child(Common.KEY_AllSALON_REFERANCE)
                            .child(Common.currentSalon.getSalonId())
                            .child(Common.KEY_BARBER_REFERANCE)
                            .child(barberID)
                            .child(bookingDate);  // book date is date simpleformat with dd_MM_yyyy == 27_06_2019

                    mReferenceDate.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            if (dataSnapshot == null) {  // If do not have any appoment

                                iTimeSlotLoadListener.onTimeSoltLoadEmpty();
                            } else {
                                // If have appoiment
                                List<BookingInformation> mListTimeSlot = new ArrayList<>();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    mListTimeSlot.add(snapshot.getValue(BookingInformation.class));

                                iTimeSlotLoadListener.onTimeSoltLoadSuccess(mListTimeSlot);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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


}
