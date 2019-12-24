package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.paperdb.Paper;

public class SettingsActivity extends AppCompatActivity {

    private Unbinder mUnbinder;

    @BindView(R.id.input_update_name)
    EditText mInputName;
    @BindView(R.id.input_update_mobile)
    EditText mInputMobile;
    @BindView(R.id.input_update_email)
    EditText mInputEmail;
    @BindView(R.id.input_update_address)
    EditText mInputAddress;
    @BindView(R.id.txt_app_version)
    TextView mAppVersion;

    @OnClick(R.id.txt_log_out)
    void btnLogOut(){

        logOut();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mUnbinder = ButterKnife.bind(this);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getAppVersion();
        loadUserInfo();
    }

    private void loadUserInfo(){

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null){

            FirebaseFirestore.getInstance().collection("AllSalon")
                    .document(Common.currentSalon.getSalonID())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot snapshot = task.getResult();
                            mInputName.setText(snapshot.getString("name"));
                            mInputMobile.setText(snapshot.getString("phone"));
                            mInputEmail.setText(snapshot.getString("email"));
                            mInputAddress.setText(snapshot.getString("address"));



                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
//        }
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
                        Intent intent = new Intent(SettingsActivity.this, SalonActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }
                })
                .setCancelable(true)
                .show();
    }

    private void getAppVersion(){
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            String appVersion = packageInfo.versionName;
            mAppVersion.setText("App Version : " +  appVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
