package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Fragments.AddStaffFragment;
import com.ahmet.barberbookingstaff.Fragments.ShowStaffFragment;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class StaffActivity extends AppCompatActivity{

   private Unbinder mUnbinder;

   @BindView(R.id.btn_app_bar)
   BottomAppBar mBottomAppBar;
   @BindView(R.id.fab_add_staff)
   FloatingActionButton mFabAddStaff;

   @OnClick(R.id.fab_add_staff)
   void addStaff(){
       Common.setFragment(new AddStaffFragment(), R.id.frame_layout_staff, getSupportFragmentManager());
   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        mUnbinder = ButterKnife.bind(this);

        Common.setFragment(new ShowStaffFragment(), R.id.frame_layout_staff, getSupportFragmentManager());

        getSupportActionBar().setTitle("Staff");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                              if (task.isSuccessful()) {
                                 DocumentSnapshot snapshot = task.getResult();
                                 if (!snapshot.get("barberType").equals("Admin")) {
                                     mFabAddStaff.setVisibility(View.GONE);
                                    // mFabAddStaff.setEnabled(true);
                                 }
                              }
                           }
                        });

        mBottomAppBar.replaceMenu(R.menu.app_bar_staff_menu);
        mBottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();
                switch (id){
                    case R.id.nav_settings_app_bar:
                        startActivity(new Intent(StaffActivity.this, SettingsActivity.class));
                        break;
                    case R.id.nav_add_product_app_bar:
                        startActivity(new Intent(StaffActivity.this, ProductsActivity.class));
                        break;
                }
                return false;
            }
        });

        mBottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.setFragment(new ShowStaffFragment(), R.id.frame_layout_staff, getSupportFragmentManager());
            }
        });

    }


}
