package com.ahmet.barberbookingstaff.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.ui.login.salon.NewSalonActivity;
import com.ahmet.barberbookingstaff.MainActivity;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    @BindView(R.id.txt_website)
    TextView mTxtWebsite;
    @BindView(R.id.txt_address)
    TextView mTxtAddress;
    @BindView(R.id.txt_app_version)
    TextView mAppVersion;
    @BindView(R.id.txt_salon_status)
    TextView mTxtSalonStatus;
    @BindView(R.id.switch_salon_status)
    Switch mSwitchSalonStatus;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @OnClick(R.id.txt_log_out)
    void btnLogOut() {

        logOut();
    }

    @OnClick(R.id.card_salon_website)
    void showDialogWebsite() {

        showDialogAddWebsite();
    }

    @OnClick(R.id.card_salon_name)
    void showDialogName() {

        showDialoUpdateName();
    }

    @OnClick(R.id.card_salon_email)
    void showDialogEmail() {

        showDialoUpdateEmail();
    }

    @OnClick(R.id.card_city)
    void showDialogCity() {

        showDialogUpdateCity();
    }

    @OnClick(R.id.card_address)
    void showDialogAddress() {

        showDialogUpdateAddress();
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
        loadSalonInfo();
    }

    private void openSalon(boolean isOpen) {

        Map<String, Object> mMapSalonStatus = new HashMap<>();
        mMapSalonStatus.put("open", isOpen);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .updateChildren(mMapSalonStatus)
                .addOnCompleteListener(task -> {
                });
    }

    private void init() {

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
    }

    private void loadSalonInfo() {


        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            Salon salon = dataSnapshot.getValue(Salon.class);
                            Common.currentSalon = salon;

                            mTxtName.setText(salon.getSalonName());
                            mTxtMobile.setText(salon.getPhone());
                            mTxtEmail.setText(salon.getEmail());
                            mTxtAddress.setText(salon.getAddress());
                            mTxtCity.setText(salon.getCity());
                            mTxtWebsite.setText(salon.getWebsite());
                            mSwitchSalonStatus.setChecked(true);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void showDialogAddWebsite() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View inflater = LayoutInflater.from(this).inflate(R.layout.dialog_add_website, null, false);

        builder.setView(inflater);
        // builder.setView(R.layout.dialog_add_website);

        EditText mInputWebsite = inflater.findViewById(R.id.input_website);
        mInputWebsite.setText(Common.currentSalon.getWebsite());

        AlertDialog dialog = builder.create();
        // custom dialog
        Common.customDialog(dialog);

        inflater.findViewById(R.id.btn_add_website).setOnClickListener(v -> {

            String website = mInputWebsite.getText().toString();

            if (TextUtils.isEmpty(website)) {
                mInputWebsite.setError(getString(R.string.please_enter_website));
                return;
            } else
                AddWebsite(website);
            dialog.dismiss();
        });

        inflater.findViewById(R.id.btn_cancel_update_website)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });



        dialog.show();
    }

    private void showDialoUpdateEmail()     {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_update_email, null, false);
        builder.setView(layoutView);

        EditText mInputEmail = layoutView.findViewById(R.id.input_update_email);
        mInputEmail.setText(Common.currentSalon.getEmail());

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);

        layoutView.findViewById(R.id.btn_update_email)
                .setOnClickListener(v -> {

                    String salonEmail = mInputEmail.getText().toString();

                    if (TextUtils.isEmpty(salonEmail)) {
                        mInputEmail.setError(getString(R.string.please_enter_salon_name));
                        return;
                    } else
                        updateEmail(salonEmail);
                    dialog.dismiss();
                });

        layoutView.findViewById(R.id.btn_cancel_update_email)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();


    }

    private void showDialoUpdateName() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_update_name, null, false);
        builder.setView(layoutView);

        EditText mInputSalonName = layoutView.findViewById(R.id.input_update_salon_name);
        mInputSalonName.setText(Common.currentSalon.getSalonName());

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);

        layoutView.findViewById(R.id.btn_update_salon_name)
                .setOnClickListener(v -> {

                    String salonName = mInputSalonName.getText().toString();

                    if (TextUtils.isEmpty(salonName)) {
                        mInputSalonName.setError(getString(R.string.please_enter_salon_name));
                        return;
                    } else
                        updateSalonName(salonName);
                    dialog.dismiss();
                });

        layoutView.findViewById(R.id.btn_cancel_update_salon_name)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();


    }

    private void showDialogUpdateCity() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_update_city, null, false);

        builder.setView(layoutView);

        EditText mInputCity = layoutView.findViewById(R.id.input_update_salon_city);
        mInputCity.setText(Common.currentSalon.getCity());

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);

        layoutView.findViewById(R.id.btn_update_salon_city)
                .setOnClickListener(v -> {

                    String city = mInputCity.getText().toString();

                    if (TextUtils.isEmpty(city)) {
                        mInputCity.setError(getString(R.string.please_enter_salon_city));
                        return;
                    } else
                        updateCity(city);
                    dialog.dismiss();
                });

        layoutView.findViewById(R.id.btn_cancel_update_salon_city)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();
    }

    private void showDialogUpdateAddress() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_update_address, null, false);

        builder.setView(layoutView);

        EditText mInputAddress = layoutView.findViewById(R.id.input_update_salon_address);
        mInputAddress.setText(Common.currentSalon.getAddress());

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);


        layoutView.findViewById(R.id.btn_update_salon_address)
                .setOnClickListener(v -> {

                    String address = mInputAddress.getText().toString();

                    if (TextUtils.isEmpty(address)) {
                        mInputAddress.setError(getString(R.string.please_enter_salon_address));
                        return;
                    } else
                        updateAddress(address);
                    dialog.dismiss();
                });

        layoutView.findViewById(R.id.btn_cancel_update_address)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();

    }

    private void updateSalonName(String salonName) {

        progressDialog.show();

        Map<String, Object> mMapSAlonName = new HashMap<>();
        mMapSAlonName.put("name", salonName);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .updateChildren(mMapSAlonName)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, getString(R.string.update_salon_name_success), Toast.LENGTH_SHORT).show();
                        loadSalonInfo();
                    }
                });
    }

    private void updateEmail(String salonEmail) {

        progressDialog.show();

        Map<String, Object> mapEmail = new HashMap<>();
        mapEmail.put("email", salonEmail);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .updateChildren(mapEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(this, getString(R.string.update_email_success), Toast.LENGTH_SHORT).show();
                        loadSalonInfo();
                    }
                });

    }

    private void updateCity(String city) {

        progressDialog.show();

        Map<String, Object> mMapCity = new HashMap<>();
        mMapCity.put("city", city);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .updateChildren(mMapCity)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, getString(R.string.update_city_success), Toast.LENGTH_SHORT).show();
                        loadSalonInfo();
                    }
                });

    }

    private void addCountry(String country) {

        progressDialog.show();

        Map<String, Object> mMapCountry = new HashMap<>();
        mMapCountry.put("country", country);

        FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
                .document(Common.currentSalon.getCity())

                .update(mMapCountry)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, getString(R.string.add_country_success), Toast.LENGTH_SHORT).show();
                            loadSalonInfo();
                        }
                    }
                });

    }

    private void updateAddress(String address) {

        progressDialog.show();

        Map<String, Object> mMapAddress = new HashMap<>();
        mMapAddress.put("address", address);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .updateChildren(mMapAddress)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, getString(R.string.update_salon_address_success), Toast.LENGTH_SHORT).show();
                        loadSalonInfo();
                    }
                });
    }

    private void AddWebsite(String website) {

        progressDialog.show();

        Map<String, Object> mMapWebsite = new HashMap<>();
        mMapWebsite.put("website", website);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .updateChildren(mMapWebsite)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, getString(R.string.add_website), Toast.LENGTH_SHORT).show();
                        loadSalonInfo();
                    }
                });
    }

    private void logOut() {
        // Just all remember Keys and start MainActivity
        Paper.init(this);

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.are_you_sure_log_out)
                .setCancelable(true)
                .setPositiveButton(R.string.log_out_from_salon, (dialogInterface, i) -> {

                    FirebaseAuth.getInstance().signOut();

                    Paper.book().delete(Common.KEY_LOGGED);
                    Paper.book().delete(Common.KEY_SALON);
                    Paper.book().delete(Common.KEY_BARBER);

                    Intent intent = new Intent(SettingsActivity.this, NewSalonActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }).setNegativeButton(R.string.log_out_from_barber, (dialog, which) -> {

                    Paper.book().delete(Common.KEY_LOGGED);
                    Paper.book().delete(Common.KEY_SALON);
                    Paper.book().delete(Common.KEY_BARBER);

                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }).setNeutralButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).show();
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
