package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.TimeSlotAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.Interface.INotificationCountListener;
import com.ahmet.barberbookingstaff.Interface.ITimeSlotLoadListener;
import com.ahmet.barberbookingstaff.Model.BookingInformation;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

import static com.ahmet.barberbookingstaff.Common.Common.mSimpleDateFormat;

public class HomeStaffActivity extends AppCompatActivity
        implements ITimeSlotLoadListener, INotificationCountListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.recycler_time_solt)
    RecyclerView mRecyclerTimeSolt;
    @BindView(R.id.calendar_time_slot)
    HorizontalCalendarView mCalendarDateView;

    private CollectionReference notificationCollectionRef;
    private CollectionReference currentBookingDateCollectionRef;
    // Copy Code from Booking Barber App (Client App)
    private DocumentReference mDocReferenceBarber;

    private EventListener<QuerySnapshot> notificationEventListener;
    private EventListener<QuerySnapshot> bookingEventListener;

    private ListenerRegistration notificationListener;
    private ListenerRegistration bookingRealTimeListener;

    private ActionBarDrawerToggle mActionBarDrawerToggle;


    // init interface
    private ITimeSlotLoadListener iTimeSlotLoadListener;
    private INotificationCountListener iNotificationCountListener;

    private AlertDialog mDialog;

    private TextView mTxtCountNotification;


    /* ======================================================================================================================
        *This is custome layout for Bottom Navigation View to show Badge
        *
        * BottomNavigationMenuView mNavigationMenu = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);
        * View navigationView = mNavigationMenu.getChildAt(1);
        * BottomNavigationItemView mNavigationItem = (BottomNavigationItemView) navigationView;
        * View lauoutView = LayoutInflater.from(this)
        *       .inflate(R.layout.action_notification, mNavigationMenu, false);
        * mNavigationItem.addView(lauoutView);
        * mTxtCountNotification = lauoutView.findViewById(R.id.count_notification);
        * mTxtCountNotification.setText("8");
      ====================================================================================================================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_staff);

        ButterKnife.bind(this);

        getSupportActionBar().setTitle(Common.currentSalon.getName());

        init();
        initView();

    }

    private void init() {

        iTimeSlotLoadListener = this;
        iNotificationCountListener = this;

        initNotificationsRealTimeUpdate();

        initBookingRealTimeUpdate();
    }

    private void initBookingRealTimeUpdate() {

        // /AllSalon/Gaza/Branch/AFXjgtlJwztf7cLFumNT/Barber/utQmhc07WVjaZdr9tbRB
        mDocReferenceBarber = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberID());

        // get current Date
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DATE, 0);
        bookingEventListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                // If have any new booking, update adapter
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                        mSimpleDateFormat.format(currentDate.getTime()));
            }
        };

        currentBookingDateCollectionRef = mDocReferenceBarber
                .collection(mSimpleDateFormat.format(currentDate.getTime()));
        bookingRealTimeListener = currentBookingDateCollectionRef.addSnapshotListener(bookingEventListener);

    }

    private void initView() {

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_exit)
                    logOut();
                return true;
            }
        });

        // get Barber Name
        View headerView = mNavigationView.getHeaderView(0);
        TextView mTxtBarberName = headerView.findViewById(R.id.txt_barber_name);
        TextView mTxtSalonName = headerView.findViewById(R.id.txt_salon_name);

        mTxtBarberName.setText(Common.currentBarber.getName());
        mTxtSalonName.setText(Common.currentSalon.getName());

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Please wait...")
                .build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);  // Add current date

        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                Common.mSimpleDateFormat.format(date.getTime()));

        // Recycler View
        mRecyclerTimeSolt.setHasFixedSize(true);
        mRecyclerTimeSolt.setLayoutManager(new StaggeredGridLayoutManager(3, LinearLayout.VERTICAL));
        mRecyclerTimeSolt.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);  // 2 day left

        HorizontalCalendar mCalendarDate = new HorizontalCalendar.Builder(this, R.id.calendar_time_slot)
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

    private void initNotificationsRealTimeUpdate() {

        notificationCollectionRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberID())
                .collection("Notifications");

        notificationEventListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots.size() > 0)
                    loadNotifications();
            }
        };

        // only listen and count all notifications
        notificationListener = notificationCollectionRef.whereEqualTo("read", false)
                .addSnapshotListener(notificationEventListener);


    }

    private void logOut() {
        // Just all remember Keys and start MainActivity
        Paper.init(this);
        Paper.book().delete(Common.KEY_LOGGED);
        Paper.book().delete(Common.KEY_SALON);
        Paper.book().delete(Common.KEY_BARBER);

        new AlertDialog.Builder(this)
                .setMessage("Are you sure want to Log Out")
                .setCancelable(false)
                .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(HomeStaffActivity.this, SalonActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void loadAvailableTimeSlotOfBarber(String barberID, String bookingDate) {

        mDialog.show();


        // Get informatio for this barber
        mDocReferenceBarber.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) { // If babrber available

                                // Get information of booking
                                // If not created return empty
                                CollectionReference mReferenceDate = FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.currentSalon.getSalonID())
                                        .collection("Barber")
                                        .document(barberID)
                                        .collection(bookingDate);  // book date is date simpleformat with dd_MM_yyyy == 27_06_2019

                                mReferenceDate.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                if (task.isSuccessful()) {

                                                    QuerySnapshot querySnapshot = task.getResult();
                                                    if (querySnapshot.isEmpty()) {  // If do not have any appoment

                                                        iTimeSlotLoadListener.onTimeSoltLoadEmpty();
                                                    } else {
                                                        // If have appoiment
                                                        List<BookingInformation> mListTimeSlot = new ArrayList<>();
                                                        for (QueryDocumentSnapshot snapshot : task.getResult())
                                                            mListTimeSlot.add(snapshot.toObject(BookingInformation.class));

                                                        iTimeSlotLoadListener.onTimeSoltLoadSuccess(mListTimeSlot);
                                                    }
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        iTimeSlotLoadListener.onTimeSoltLoadFailed(e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        if (item.getItemId() == R.id.action_new_notification){
            startActivity(new Intent(HomeStaffActivity.this, NotificationsActivity.class));
            mTxtCountNotification.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setMessage("Are you sure want to exit")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(HomeStaffActivity.this, "Fack function exit", Toast.LENGTH_SHORT).show();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onTimeSoltLoadSuccess(List<BookingInformation> mListTimeSlot) {

        TimeSlotAdapter mTimeSlotAdapter = new TimeSlotAdapter(this, mListTimeSlot);
        mRecyclerTimeSolt.setAdapter(mTimeSlotAdapter);

        mDialog.dismiss();
    }

    @Override
    public void onTimeSoltLoadFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeSoltLoadEmpty() {

        TimeSlotAdapter mTimeSlotAdapter = new TimeSlotAdapter(this);
        mRecyclerTimeSolt.setAdapter(mTimeSlotAdapter);

        mDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.notification_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_new_notification);


            mTxtCountNotification = menuItem.getActionView().findViewById(R.id.count_notification);

            loadNotifications();

            menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onOptionsItemSelected(menuItem);
                }
            });

        return super.onCreateOptionsMenu(menu);
    }


    private void loadNotifications() {

        notificationCollectionRef.whereEqualTo("read", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){

                            iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeStaffActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
        });
    }

    @Override
    public void onNotificationCountSuccess(int count) {

        if (count == 0)
            mTxtCountNotification.setVisibility(View.INVISIBLE);
        else {

            mTxtCountNotification.setVisibility(View.VISIBLE);
            if (count <= 9)
                mTxtCountNotification.setText(String.valueOf(count));
            else
                mTxtCountNotification.setText("9+");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealTimeUpdate();
        initNotificationsRealTimeUpdate();
    }

    @Override
    protected void onStop() {

        if(notificationListener != null)
            notificationListener.remove();

        if (bookingRealTimeListener != null)
            bookingRealTimeListener.remove();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if(notificationListener != null)
            notificationListener.remove();

        if (bookingRealTimeListener != null)
            bookingRealTimeListener.remove();


        super.onDestroy();
    }
}
