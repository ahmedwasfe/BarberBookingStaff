package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.ui.home.HomeFragment;
import com.ahmet.barberbookingstaff.ui.product.ProductFragment;
import com.ahmet.barberbookingstaff.ui.service.ServicesFragment;
import com.ahmet.barberbookingstaff.ui.staff.StaffFragment;
import com.ahmet.barberbookingstaff.callback.INotificationCountListener;
import com.ahmet.barberbookingstaff.ui.settings.SettingsBarberActivity;
import com.ahmet.barberbookingstaff.ui.notification.NotificationsActivity;
import com.ahmet.barberbookingstaff.ui.settings.SettingsActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

import static com.ahmet.barberbookingstaff.common.Common.setFragment;

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

    private DatabaseReference notificationRef;

    // Copy Code from Booking Barber App (Client App)
    private EventListener<QuerySnapshot> notificationEventListener;


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

        Paper.init(HomeStaffActivity.this);
        Paper.book().write(Common.KEY_LOGGED, Common.currentBarber.getUsername());

       setFragment(HomeFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());

       initView();

    }


    private void initView() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(Common.currentSalon.getSalonName());
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
                setFragment(StaffFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_show_product) {
                mDrawerLayout.closeDrawers();
                setFragment(ProductFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
            } else if (menuItem.getItemId() == R.id.nav_add_service) {
                mDrawerLayout.closeDrawers();
                setFragment(ServicesFragment.getInstance(), R.id.frame_layout_home, getSupportFragmentManager());
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
        ConstraintLayout mConstraintBarber = headerView.findViewById(R.id.constraint_barber);
        TextView mTxtStaffName = headerView.findViewById(R.id.txt_barber_name);
        TextView mTxtStaffType = headerView.findViewById(R.id.txt_barber_type);
        CircleImageView mImageSalon = headerView.findViewById(R.id.img_salon);

        mTxtStaffName.setText(Common.currentBarber.getName());
        mTxtStaffType.setText(Common.currentBarber.getBarberType());
        Picasso.get()
                .load(Common.currentBarber.getImage())
                .placeholder(R.drawable.hairdresser)
                .into(mImageSalon);

        mConstraintBarber.setOnClickListener(v -> startActivity(new Intent(HomeStaffActivity.this, SettingsBarberActivity.class)));

      //  getSalonType(mImageSalon);

    }


    private void getSalonType(CircleImageView mImageSalon){

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){


                            if (dataSnapshot.child("salonType").getValue().equals(getString(R.string.men)))
                                Picasso.get().load(R.drawable.hairdresser).into(mImageSalon);
                            else if (dataSnapshot.child("salonType").getValue().equals(getString(R.string.women)))
                                Picasso.get().load(R.drawable.women_salon).into(mImageSalon);
                            else if (dataSnapshot.child("salonType").getValue().equals(getString(R.string.both)))
                                Picasso.get().load(R.drawable.hairdresser).into(mImageSalon);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.notification_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_new_notification);


            mTxtCountNotification = menuItem.getActionView().findViewById(R.id.count_notification);

          //  loadNotifications();

            menuItem.getActionView().setOnClickListener(view -> onOptionsItemSelected(menuItem));

        return super.onCreateOptionsMenu(menu);
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


}