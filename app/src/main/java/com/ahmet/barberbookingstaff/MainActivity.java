package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.Salon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.input_login_barber_username)
    EditText mInputUsername;
    @BindView(R.id.input_login_password)
    EditText mInputPassword;

    @BindView(R.id.btn_login)
    Button mBtnLogin;

    @BindView(R.id.progress_login)
    RotateLoading mRotateLoading;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser firebaseUser;

    @OnClick(R.id.btn_login)
    void onLoginClick() {

        mRotateLoading.start();


        String username = mInputUsername.getText().toString();
        String password = mInputPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            mInputUsername.setError(getString(R.string.please_enter_username));
            mRotateLoading.stop();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mInputPassword.setError(getString(R.string.please_enter_password));
            mRotateLoading.stop();
            return;
        }

        if (password.length() < 8) {
            mInputPassword.setError(getString(R.string.password_must_be_at_least_6));
            mRotateLoading.stop();
            return;
        }

        checkBarberExsist(username, password);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mRotateLoading.stop();

        init();

        Paper.init(this);
        String username = Paper.book().read(Common.KEY_LOGGED);
        if (username != null && firebaseUser.getUid() != null){
            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_AllSALON_REFERANCE)
                    .child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){

                                mRotateLoading.start();
                                mInputUsername.setVisibility(View.GONE);
                                mInputPassword.setVisibility(View.GONE);
                                mBtnLogin.setVisibility(View.GONE);
                                Salon salon = dataSnapshot.getValue(Salon.class);
                                Common.currentSalon = salon;
                                startActivity(new Intent(MainActivity.this, HomeStaffActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }else{
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
        }



    }

    private void init() {

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
    }

    private void checkBarberExsist(String username, String password) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                        if (dataSnapshot1.exists()){

                            Salon salon = dataSnapshot1.getValue(Salon.class);
                            getSupportActionBar().setTitle(salon.getSalonName());

                            FirebaseDatabase.getInstance().getReference()
                                    .child(Common.KEY_AllSALON_REFERANCE)
                                    .child(firebaseUser.getUid())
                                    .child(Common.KEY_BARBER_REFERANCE)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.exists()) {
                                                        if (snapshot.child("username").getValue().equals(username) &&
                                                                snapshot.child("password").getValue().equals(password)) {

                                                            mRotateLoading.stop();
                                                            Barber barber = snapshot.getValue(Barber.class);
                                                            gotoHomeActivity(salon, barber);
                                                            Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            mRotateLoading.stop();
                                                          //  Toast.makeText(MainActivity.this, "username or password is incorrect", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }else {
                                                        mRotateLoading.stop();
                                                        Toast.makeText(MainActivity.this, "Username not exixst", Toast.LENGTH_SHORT).show();
                                                    }
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

    private void gotoHomeActivity(Salon salon, Barber barber) {

        Common.currentSalon = salon;
        Common.currentBarber = barber;
        startActivity(new Intent(this, HomeStaffActivity.class));
        finish();
    }



}