package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Fragments.AddProductFragment;
import com.ahmet.barberbookingstaff.Fragments.AddServiceFragmnet;
import com.ahmet.barberbookingstaff.Fragments.AddStaffFragment;
import com.ahmet.barberbookingstaff.Fragments.HomeFragment;
import com.ahmet.barberbookingstaff.Fragments.ShowProductFragment;
import com.ahmet.barberbookingstaff.Fragments.ShowStaffFragment;
import com.ahmet.barberbookingstaff.Interface.INotificationCountListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.SubActivity.NotificationsActivity;
import com.ahmet.barberbookingstaff.SubActivity.SettingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

import static com.ahmet.barberbookingstaff.Common.Common.setFragment;

public class HomeStaffActivity extends AppCompatActivity
        implements INotificationCountListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.tool_bar_home)
    Toolbar mToolbar;
    @BindView(R.id.text_message_verification_email)
    TextView mTxtMessageVerificationEmail;

    private CollectionReference notificationCollectionRef;

    // Copy Code from Booking Barber App (Client App)
    private EventListener<QuerySnapshot> notificationEventListener;

    private ListenerRegistration notificationListener;

    private ActionBarDrawerToggle mActionBarDrawerToggle;


    // init interface
    private INotificationCountListener iNotificationCountListener;

    private AlertDialog mDialog;

    private TextView mTxtCountNotification;

    private FirebaseAuth mAuth;


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


        setFragment(HomeFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());

//        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
//                .document(Common.currentSalon.getSalonID())
//                .collection(Common.KEY_COLLECTION_BARBER)
//                .get()
//                .addOnCompleteListener(task -> {
//
//                    if (task.isSuccessful()){
//                        if (task.getResult().size() > 0) {
//                            Barber barber = new Barber();
//                            for (DocumentSnapshot snapshot : task.getResult()) {
//                                barber = snapshot.toObject(Barber.class);
//                                barber.setBarberID(snapshot.getId());
//                                //Log.e("ID From App", Common.currentBarber.getBarberID());
//                               // Log.e("ID From Database", snapshot.getId());
//
//                            }
//                        }
//                    }
//                });

        init();
        initView();

//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (user.isEmailVerified())
//            mTxtMessageVerificationEmail.setVisibility(View.GONE);
//        else
//            mTxtMessageVerificationEmail.setVisibility(View.VISIBLE);

    }

    private void init() {

        iNotificationCountListener = this;

        mAuth = FirebaseAuth.getInstance();

        initNotificationsRealTimeUpdate();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage(R.string.please_wait)
                .build();
    }


    private void initView() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(Common.currentSalon.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();


        mNavigationView.setNavigationItemSelectedListener(menuItem -> {

            if (menuItem.getItemId() == R.id.nav_home) {
                mDrawerLayout.closeDrawers();
                setFragment(HomeFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_show_barber) {
                mDrawerLayout.closeDrawers();
                setFragment(ShowStaffFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_add_barber) {
                mDrawerLayout.closeDrawers();
                setFragment(AddStaffFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_show_product) {
                mDrawerLayout.closeDrawers();
                setFragment(ShowProductFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_add_product) {
                mDrawerLayout.closeDrawers();
                setFragment(AddProductFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_add_service) {
                mDrawerLayout.closeDrawers();
                setFragment(AddServiceFragmnet.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_notifications) {
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(HomeStaffActivity.this, NotificationsActivity.class));
            } else if (menuItem.getItemId() == R.id.nav_help) {
                Toast.makeText(this, getString(R.string.help), Toast.LENGTH_SHORT).show();
            } else if (menuItem.getItemId() == R.id.nav_settings) {
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(HomeStaffActivity.this, SettingsActivity.class));
            }

            return true;
        });

        // get Barber Name
        View headerView = mNavigationView.getHeaderView(0);
        TextView mTxtStaffName = headerView.findViewById(R.id.txt_barber_name);
        TextView mTxtStaffType = headerView.findViewById(R.id.txt_barber_type);
        CircleImageView mImageSalon = headerView.findViewById(R.id.img_salon);

        getSalonType(mImageSalon);
        loadBarberInfo(mTxtStaffName, mTxtStaffType);
    }

    private void loadBarberInfo(TextView mTxtStaffName, TextView mTxtStaffType) {


        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .collection(Common.KEY_COLLECTION_BARBER)
                .document(Common.currentBarber.getBarberID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()){
                            DocumentSnapshot snapshot = task.getResult();
                            mTxtStaffName.setText(snapshot.getString("name"));
                            mTxtStaffType.setText(snapshot.getString("barberType"));
                        }
                    }
                });
    }

    private void getSalonType(CircleImageView mImageSalon){

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()){

                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.getString("salonType").equals(getString(R.string.men)))
                                Picasso.get().load(R.drawable.hairdresser).into(mImageSalon);
                            else if (snapshot.getString("salonType").equals(getString(R.string.women)))
                                Picasso.get().load(R.drawable.women_salon).into(mImageSalon);
                            else if (snapshot.getString("salonType").equals(getString(R.string.both)))
                                Picasso.get().load(R.drawable.hairdresser).into(mImageSalon);
                        }
                    }
                });

    }

    private void initNotificationsRealTimeUpdate() {

        notificationCollectionRef = FirebaseFirestore.getInstance()
                .collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .collection(Common.KEY_COLLECTION_BARBER)
                .document(Common.currentBarber.getBarberID())
                .collection(Common.KEY_COLLECTION_NOTIFICATIONS);

        notificationEventListener = (queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.size() > 0)
                loadNotifications();
        };

        // only listen and count all notifications
        notificationListener = notificationCollectionRef.whereEqualTo("read", false)
                .addSnapshotListener(notificationEventListener);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        if (item.getItemId() == R.id.action_new_notification){
            startActivity(new Intent(HomeStaffActivity.this, NotificationsActivity.class));
            mTxtCountNotification.setText("0");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(R.string.are_you_sure_to_exit)
                .setCancelable(true)
                .setPositiveButton(R.string.exit, (dialogInterface, i) -> Toast.makeText(HomeStaffActivity.this, "Fack function exit", Toast.LENGTH_SHORT))
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.notification_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_new_notification);


            mTxtCountNotification = menuItem.getActionView().findViewById(R.id.count_notification);

            loadNotifications();

            menuItem.getActionView().setOnClickListener(view -> onOptionsItemSelected(menuItem));

        return super.onCreateOptionsMenu(menu);
    }

    private void loadNotifications() {

        notificationCollectionRef.whereEqualTo("read", false)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                    }

                }).addOnFailureListener(e -> Toast.makeText(HomeStaffActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
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
                mTxtCountNotification.setText(R.string.notification_count);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initNotificationsRealTimeUpdate();
    }

    @Override
    protected void onStop() {

        if(notificationListener != null)
            notificationListener.remove();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if(notificationListener != null)
            notificationListener.remove();

        super.onDestroy();
    }
}