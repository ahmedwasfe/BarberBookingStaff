package com.ahmet.barberbookingstaff.ui.login.barber.forgotPassword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.CheckBarber;
import com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp.VerifyOTPActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ForgotPasswordActivity.class.getSimpleName();


    @BindView(R.id.btn_back_arrow)
    ImageView imgBack;
    @BindView(R.id.text_forgot_password)
    TextView textForgotPassword;
    @BindView(R.id.txt_input_phone)
    TextInputLayout textInputPhone;
    @BindView(R.id.ccp)
    CountryCodePicker ccp;
    @BindView(R.id.relative_progress)
    RelativeLayout progress;

    private IBarbersAPI sService;
    private Settings settings;

    @OnClick(R.id.btn_send_code)
    void onSendCodeClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snack_error_layout, findViewById(R.id.forgot_password_container),
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }

        if (!validatePhone())
            return;

        progress.setVisibility(View.VISIBLE);

        String phone = textInputPhone.getEditText().getText().toString().trim();
        String phoneNumber = "+" + ccp.getSelectedCountryCode() + phone;

        sService.checkBarberExists(phoneNumber)
                .enqueue(new Callback<CheckBarber>() {
                    @Override
                    public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {
                        if (response.body().isExists()){

                            textInputPhone.setError(null);
                            textInputPhone.setErrorEnabled(false);
                            Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOTPActivity.class);
                            intent.putExtra(Common.KEY_PHONE_SIGNUP, phoneNumber);
                            intent.putExtra(Common.KEY_WAHT_TO_DO, Common.KEY_UPDATE_PASSWORD);
                            Pair[] pairs = new Pair[3];
                            pairs[0] = new Pair<View, String>(imgBack, "transition_back_arrow");
                            pairs[1] = new Pair<View, String>(textForgotPassword, "transition_title_text");
                            pairs[2] = new Pair<View, String>(findViewById(R.id.btn_send_code), "transition_create_account");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ForgotPasswordActivity.this, pairs);
                                startActivity(intent, options.toBundle());
                                finish();
                            } else {
                                startActivity(intent);
                                finish();
                            }
                            progress.setVisibility(View.GONE);
                        }else{
                            progress.setVisibility(View.GONE);
                            textInputPhone.setError(getString(R.string.barber_not_exsist));
                            textInputPhone.requestFocus();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckBarber> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    @OnClick(R.id.btn_back_arrow)
    void onBackClick(){
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {

        sService = Common.getBarbersAPI();
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
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }
}