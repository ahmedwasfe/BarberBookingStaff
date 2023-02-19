package com.ahmet.barberbookingstaff.ui.login.barber.forgotPassword;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.CheckBarber;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = UpdatePasswordActivity.class.getSimpleName();

    @BindView(R.id.txt_input_new_password)
    TextInputLayout txtNewPassword;
    @BindView(R.id.txt_input_confirm_new_password)
    TextInputLayout txtConfirmNewPassword;
    @BindView(R.id.relative_progress)
    RelativeLayout progress;

    private String phoneNumber;
    private IBarbersAPI sService;
    private Settings settings;

    @OnClick(R.id.btn_reset_password)
    void onUpdatePasswordClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snack_error_layout, findViewById(R.id.update_password_countainer),
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }

        if (!validatePassword())
            return;

        String password = txtNewPassword.getEditText().getText().toString().trim();
        progress.setVisibility(View.VISIBLE);
        updatePassword(phoneNumber, password);

    }

    private void updatePassword(String phoneNumber, String password) {

        sService.checkBarberExists(phoneNumber)
                .enqueue(new Callback<CheckBarber>() {
                    @Override
                    public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {
                        if (response.body().isExists()){
                            sService.updatePassword(phoneNumber, password)
                                    .enqueue(new Callback<Barber>() {
                                        @Override
                                        public void onResponse(Call<Barber> call, Response<Barber> response) {
                                            startActivity(new Intent(UpdatePasswordActivity.this,
                                                    UpdatePasswordSuccess.class));
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(Call<Barber> call, Throwable t) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckBarber> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        ButterKnife.bind(this);
        initUI();
    }

    private void initUI() {

        sService = Common.getBarbersAPI();
        settings = new Settings(this);
        progress.setVisibility(View.GONE);

        if (getIntent() != null)
            phoneNumber = getIntent().getStringExtra(Common.KEY_PHONE_SIGNUP);
    }

    private boolean validatePassword(){

        String password = txtNewPassword.getEditText().getText().toString().trim();
        String confirmPassword = txtConfirmNewPassword.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(password)){
            txtNewPassword.setError(getString(R.string.please_enter_password));
            return false;
        }else if (password.length() < 6){
            txtNewPassword.setError(getString(R.string.password_must_be_at_least_6));
            return false;
        }else if (TextUtils.isEmpty(confirmPassword)){
            txtConfirmNewPassword.setError(getString(R.string.please_enter_confirm_password));
            return false;
        }else if (confirmPassword.length() < 6){
            txtConfirmNewPassword.setError(getString(R.string.password_must_be_at_least_6));
            return false;
        }else if (!password.equals(confirmPassword)){
            txtConfirmNewPassword.setError(getString(R.string.the_two_passwords_do_not_match));
            return false;
        }else{
            txtNewPassword.setError(null);
            txtConfirmNewPassword.setError(null);
            txtNewPassword.setErrorEnabled(false);
            txtConfirmNewPassword.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }
}