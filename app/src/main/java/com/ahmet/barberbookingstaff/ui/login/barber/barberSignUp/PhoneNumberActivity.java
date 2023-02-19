package com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.CheckBarber;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneNumberActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PhoneNumberActivity.class.getSimpleName();

    @BindView(R.id.text_create_account)
    TextView textCreateAccount;
    @BindView(R.id.text_numbers_of_screens)
    TextView textScreens;
    @BindView(R.id.btn_back_arrow)
    ImageView imgBack;
    @BindView(R.id.btn_send_code)
    Button sendCode;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.txt_input_phone)
    TextInputLayout textInputPhone;
    @BindView(R.id.ccp)
    CountryCodePicker ccp;
    @BindView(R.id.relative_progress)
    RelativeLayout progress;
    @BindView(R.id.tv_salon_name)
    TextView txtSalonName;

    private String _salonEmail, _fullName, _username, _password;
    private int _gender;

    private IBarbersAPI sService;
    private Settings settings;

    @OnClick(R.id.btn_send_code)
    void onSendCodeClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snack_error_layout, findViewById(R.id.phone_container),
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }

        if (!validatePhone())
            return;

        String _phone = textInputPhone.getEditText().getText().toString().trim();
        String _phoneNumber = "+" + ccp.getSelectedCountryCode() + _phone;
        Log.d(TAG, "onSendCodeClick: " + _phoneNumber);

        checkBarberExsist(_phoneNumber);

    }

    @OnClick(R.id.btn_back_arrow)
    void onBackClick(){
        onBackPressed();
    }

    private void checkBarberExsist(String phoneNumber) {

        progress.setVisibility(View.VISIBLE);

        sService.checkBarberExists(phoneNumber)
                .enqueue(new Callback<CheckBarber>() {
                    @Override
                    public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {
                        if (response.body().isExists()){
                            progress.setVisibility(View.GONE);
                            Toast.makeText(PhoneNumberActivity.this, "This phone exisit", Toast.LENGTH_SHORT).show();
                        }else{
                            progress.setVisibility(View.GONE);
                            goToVerifyPhone(phoneNumber);
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckBarber> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    private void goToVerifyPhone(String phoneNumber) {

        Intent intent = new Intent(this, VerifyOTPActivity.class);
        intent.putExtra(Common.KEY_EMAIL_SIGNUP, _salonEmail);
        intent.putExtra(Common.KEY_FULLNAME_SIGNUP, _fullName);
        intent.putExtra(Common.KEY_USERNAME_SIGNUP, _username);
        intent.putExtra(Common.KEY_PASSWORD_SIGNUP, _password);
        intent.putExtra(Common.KEY_GENDER_SIGNUP, _gender);
        intent.putExtra(Common.KEY_PHONE_SIGNUP, phoneNumber);

        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.phone_container), "transition_otp_verify");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        } else{
            startActivity(intent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);
        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {

        if (getIntent() != null) {
            _salonEmail = getIntent().getStringExtra(Common.KEY_EMAIL_SIGNUP);
            _fullName = getIntent().getStringExtra(Common.KEY_FULLNAME_SIGNUP);
            _username = getIntent().getStringExtra(Common.KEY_USERNAME_SIGNUP);
            _password = getIntent().getStringExtra(Common.KEY_PASSWORD_SIGNUP);
            _gender = getIntent().getIntExtra(Common.KEY_GENDER_SIGNUP, 0);
        }

       sService = Common.getBarbersAPI();
       Common.getSalonName(sService, txtSalonName, _salonEmail);
       settings = new Settings(this);
       progress.setVisibility(View.GONE);
    }

    private boolean validatePhone() {
        String fullName = textInputPhone.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            textInputPhone.setError(getString(R.string.please_enter_phone_number));
            return false;
        } else {
            textInputPhone.setError(null);
            textInputPhone.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_IP_SETTINGS));
    }
}