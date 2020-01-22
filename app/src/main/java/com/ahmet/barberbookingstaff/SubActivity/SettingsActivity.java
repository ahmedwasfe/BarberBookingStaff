package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class SettingsActivity extends AppCompatActivity {


    @BindView(R.id.scroll_view_settings)
    NestedScrollView mScrollView;
    @BindView(R.id.txt_name)
    TextView mTxtName;
    @BindView(R.id.txt_mobile)
    TextView mTxtMobile;
    @BindView(R.id.txt_email)
    TextView mTxtEmail;
    @BindView(R.id.txt_verification_email)
    TextView mTxtVerificationEmail;
    @BindView(R.id.txt_city)
    TextView mTxtCity;
    @BindView(R.id.txt_address)
    TextView mTxtAddress;
    @BindView(R.id.txt_app_version)
    TextView mAppVersion;
    @BindView(R.id.txt_salon_status)
    TextView mTxtSalonStatus;
    @BindView(R.id.switch_salon_status)
    Switch mSwitchSalonStatus;

    private FirebaseAuth mAuth;

    @OnClick(R.id.txt_log_out)
    void btnLogOut() {

        logOut();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);
        getSupportActionBar().setTitle(R.string.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

//        if (mAuth.getCurrentUser().isEmailVerified())
//            mTxtVerificationEmail.setVisibility(View.GONE);
//        else
//            mTxtVerificationEmail.setVisibility(View.VISIBLE);


        mSwitchSalonStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                openSalon(true);
                mTxtSalonStatus.setText(getString(R.string.open_salon));
            } else {
                openSalon(false);
                mTxtSalonStatus.setText(getString(R.string.close_salon));
            }
        });

        getAppVersion();
        loadUserInfo();
    }

    private void openSalon(boolean isOpen) {

        Map<String, Object> mMapSalonStatus = new HashMap<>();
        mMapSalonStatus.put("open", isOpen);

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .update(mMapSalonStatus)
                .addOnCompleteListener(task -> {

//                    FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
//                            .document(Common.currentSalon.getSalonID())
//                            .get()
//                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    if (task.isSuccessful()){
//
//                                        DocumentSnapshot snapshot = task.getResult();
//                                        boolean isOpen = snapshot.getBoolean("open");
//                                        if (isOpen)
//                                            Common.showSnackBar(SettingsActivity.this, mScrollView, getString(R.string.open_salon));
//                                        else
//                                            Common.showSnackBar(SettingsActivity.this, mScrollView, getString(R.string.close_salon));
//
//                                    }
//                                }
//                            });

                    //Common.showSnackBar(SettingsActivity.this, mScrollView, getString(R.string.sucess));
    });
}

    private void init() {

        mAuth = FirebaseAuth.getInstance();
    }

    private void loadUserInfo() {

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null){

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .get()
                .addOnCompleteListener(task -> {

                    DocumentSnapshot snapshot = task.getResult();
                    mTxtName.setText(snapshot.getString("name"));
                    mTxtMobile.setText(snapshot.getString("phone"));
                    mTxtEmail.setText(snapshot.getString("email"));
                    mTxtAddress.setText(snapshot.getString("address"));
                    mTxtCity.setText(snapshot.getString("city"));
                    mSwitchSalonStatus.setChecked(snapshot.getBoolean("open"));
//                    boolean isOpen = snapshot.getBoolean("open");

//                    if (isOpen) {
//                        mSwitchSalonStatus.setChecked(true);
//                        mTxtSalonStatus.setText(getString(R.string.open_salon));
//                    }else {
//                        mSwitchSalonStatus.setChecked(false);
//                        mTxtSalonStatus.setText(getString(R.string.close_salon));
//                    }


                }).addOnFailureListener(e ->
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logOut() {
        // Just all remember Keys and start MainActivity
        Paper.init(this);
        Paper.book().delete(Common.KEY_LOGGED);
        Paper.book().delete(Common.KEY_SALON);
        Paper.book().delete(Common.KEY_BARBER);

        new AlertDialog.Builder(this)
                .setMessage(R.string.are_you_sure_log_out)
                .setCancelable(false)
                .setPositiveButton(R.string.log_out, (dialogInterface, i) -> {

                    Intent intent = new Intent(SettingsActivity.this, SalonActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            String appVersion = packageInfo.versionName;
            mAppVersion.setText("App Version : " + appVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
