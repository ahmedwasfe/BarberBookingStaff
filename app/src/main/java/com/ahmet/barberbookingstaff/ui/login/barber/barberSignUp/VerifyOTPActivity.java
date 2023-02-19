package com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.CheckBarber;
import com.ahmet.barberbookingstaff.ui.login.barber.forgotPassword.UpdatePasswordActivity;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOTPActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = VerifyOTPActivity.class.getSimpleName();

    @BindView(R.id.timer)
    TextView tvTimer;
    @BindView(R.id.btn_resend_code)
    Button resendCode;
    @BindView(R.id.pin_view)
    PinView pinView;

    private String isUpdatePassword;
    private String _phoneNumber;
    private String _salonEmail, _fullName, _username, _password;
    private int _gender;
    private String codeBySystem;

    private CountDownTimer countTimer;

    private IBarbersAPI sService;
    private Settings settings;

    @OnClick(R.id.btn_verify_code)
    void onVerifyCodeClick() {

        Log.d(TAG, "onVerifyCodeClick: " + _phoneNumber + ": " + _salonEmail + ": " + _fullName + ": " +
                _username + ": " + _password + ": " + _gender);

        String code = pinView.getText().toString();
        if (!TextUtils.isEmpty(code)) {
            verifyCode(code);
        }
    }

    @OnClick(R.id.btn_resend_code)
    void onResendCodeClick() {
        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snack_error_layout, findViewById(R.id.otp_countainer),
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }
        sendVerificationCode(_phoneNumber);
        startTimer();
    }

    @OnClick(R.id.btn_close)
    void onCloseClick() {
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        ButterKnife.bind(this);

        initUI();
        startTimer();
        sendVerificationCode(_phoneNumber);
    }

    private void initUI() {

        sService = Common.getBarbersAPI();
        settings = new Settings(this);

        if (getIntent() != null) {
            isUpdatePassword = getIntent().getStringExtra(Common.KEY_WAHT_TO_DO);
            _phoneNumber = getIntent().getStringExtra(Common.KEY_PHONE_SIGNUP);
            _salonEmail = getIntent().getStringExtra(Common.KEY_EMAIL_SIGNUP);
            _fullName = getIntent().getStringExtra(Common.KEY_FULLNAME_SIGNUP);
            _username = getIntent().getStringExtra(Common.KEY_USERNAME_SIGNUP);
            _password = getIntent().getStringExtra(Common.KEY_PASSWORD_SIGNUP);
            _gender = getIntent().getIntExtra(Common.KEY_GENDER_SIGNUP, 0);

        }

        Log.d(TAG, "initUI: " + _phoneNumber + ": " + _salonEmail + ": " + _fullName + ": " +
                _username + ": " + _password + ": " + _gender);
    }

    private void startTimer() {

        long maxTimeInMilliseconds = 60000;
        int tick = 1000;
        countTimer = new CountDownTimer(maxTimeInMilliseconds, tick) {

            @Override
            public void onTick(long millisUntilFinished) {
                resendCode.setEnabled(false);
                long remainedSecs = millisUntilFinished / 1000;
                tvTimer.setText("" + (remainedSecs / 60) + ":" + (remainedSecs % 60));

            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                cancel();
                resendCode.setEnabled(true);
            }
        }.start();

    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    codeBySystem = s;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        pinView.setText(code);
                        verifyCode(code);
                        tvTimer.setText("00:00");
                        countTimer.cancel();
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(VerifyOTPActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onVerificationFailed: " + e.getMessage());
                }
            };

    private void verifyCode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (isUpdatePassword.equals(Common.KEY_UPDATE_PASSWORD))
                                updatePassword();
                            else
                                createNewbarbe(task.getResult().getUser().getPhoneNumber());
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void updatePassword() {

        Intent intent = new Intent(this, UpdatePasswordActivity.class);
        intent.putExtra(Common.KEY_PHONE_SIGNUP, _phoneNumber);
        startActivity(intent);
        finish();
    }

    private void createNewbarbe(String phoneNumber) {

        sService.checkBarberExists(phoneNumber)
                .enqueue(new Callback<CheckBarber>() {
                    @Override
                    public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {
                        if (!response.body().isExists()) {
                            sService.createNewBarber(_phoneNumber, _fullName, _username, _password, "", _gender, _salonEmail, 0, 1)
                                    .enqueue(new Callback<Barber>() {
                                        @Override
                                        public void onResponse(Call<Barber> call, Response<Barber> response) {
                                            if (response.isSuccessful()) {
                                                Log.d(TAG, "onResponse: " + new Gson().toJson(response.body()));
                                                Toast.makeText(VerifyOTPActivity.this, "add data success", Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Barber> call, Throwable t) {
                                            Log.e(TAG, "onAddFailure: " + t.getMessage());
                                        }
                                    });
                        } else {
                            Toast.makeText(VerifyOTPActivity.this, "This barber Exists", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckBarber> call, Throwable t) {
                        Log.e(TAG, "onCheckFailure: " + t.getMessage());
                    }
                });
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }
}