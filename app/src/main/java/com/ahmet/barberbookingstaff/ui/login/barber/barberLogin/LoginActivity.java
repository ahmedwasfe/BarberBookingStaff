package com.ahmet.barberbookingstaff.ui.login.barber.barberLogin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.WelcomeActivity;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.CheckBarber;
import com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp.CheckSalonActivity;
import com.ahmet.barberbookingstaff.ui.login.barber.forgotPassword.ForgotPasswordActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.login_container)
    ScrollView container;
    @BindView(R.id.txt_input_username_login)
    TextInputLayout txtInputUsername;
    @BindView(R.id.txt_input_password_login)
    TextInputLayout txtInputPassword;
    @BindView(R.id.relative_progress)
    RelativeLayout progress;

    private IBarbersAPI sService;
    private Settings settings;

    @OnClick(R.id.btn_back_arrow)
    void onBackLoginClick() {
        onBackPressed();
    }

    @OnClick(R.id.btn_go_to_create_account)
    void onCreateAccountClick(){
        Intent intent = new Intent(this, CheckSalonActivity.class);
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.btn_go_to_create_account), "transition_create_account");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    @OnClick(R.id.btn_go_to_forgot_password)
    void onForgotPasswordClick() {

        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.btn_go_to_forgot_password), "transition_forgot_password");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        } else
            startActivity(intent);
    }

    @OnClick(R.id.btn_login)
    void onLoginClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snacurn;k_error_layout, container,
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }

        if (!validateUsername() | !validatePassword())
            return;

        String userLogin = txtInputUsername.getEditText().getText().toString().trim();
        String password = txtInputPassword.getEditText().getText().toString().trim();

        login(userLogin, password);
    }

    private void login(String userLogin, String password) {

        progress.setVisibility(View.VISIBLE);

        sService.checkBarberUsername(userLogin)
                .enqueue(new Callback<CheckBarber>() {
                    @Override
                    public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {
                        if (response.body().isExists()) {
                            sService.loginBarber(userLogin, password)
                                    .enqueue(new Callback<Barber>() {
                                        @Override
                                        public void onResponse(Call<Barber> call, Response<Barber> response) {

                                            if (password.equals(response.body().getPassword())) {
                                                progress.setVisibility(View.GONE);
                                                Log.d(TAG, "onResponse: " + response.body().getPhone() + ": " +
                                                        response.body().getUsername() + ": " +
                                                        response.body().getPassword());
                                                goToHome(response.body());
                                            } else {
                                                progress.setVisibility(View.GONE);
                                                Common.showSnackBar(LoginActivity.this,
                                                        R.layout.snack_error_layout,
                                                        container,
                                                        getString(R.string.wrong_password));
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Barber> call, Throwable t) {
                                            progress.setVisibility(View.GONE);
                                            Log.e(TAG, "onFailureLogin: " + t.getMessage());
                                        }
                                    });

                        } else {
                            progress.setVisibility(View.GONE);
                            Common.showSnackBar(LoginActivity.this, R.layout.snack_error_layout,
                                    container, "Barber not exsist");
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckBarber> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Log.e(TAG, "onFailureCheckUsername: " + t.getMessage());
                    }
                });

        /*
        if (!userLogin.contains("+")) {

        } else {
            sService.checkBarberExists(userLogin)
                    .enqueue(new Callback<CheckBarber>() {
                        @Override
                        public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {
                            if (response.body().isExists()) {
                                sService.loginBarber(userLogin, null, password)
                                        .enqueue(new Callback<CheckBarber>() {
                                            @Override
                                            public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {
                                                if (response.body().isExists()) {
                                                    progress.setVisibility(View.GONE);
                                                    if (password.equals(response.body().getPassword())) {
                                                        Log.d(TAG, "onResponse: " + response.body().getPhone() + ": " +
                                                                response.body().getUsername() + ": " +
                                                                response.body().getPassword());
                                                        Common.showSnackBar(LoginActivity.this,
                                                                R.layout.snack_success_layout,
                                                                container,
                                                                "Login Success");
                                                    } else {
                                                        progress.setVisibility(View.GONE);
                                                        progress.setVisibility(View.GONE);
                                                        Common.showSnackBar(LoginActivity.this,
                                                                R.layout.snack_error_layout,
                                                                container,
                                                                getString(R.string.wrong_password));
                                                    }
                                                } else {
                                                    progress.setVisibility(View.GONE);
                                                    Common.showSnackBar(LoginActivity.this,
                                                            R.layout.snack_error_layout,
                                                            container,
                                                            getString(R.string.wrong_phone));
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<CheckBarber> call, Throwable t) {
                                                progress.setVisibility(View.GONE);
                                                Log.e(TAG, "onFailureLogin: " + t.getMessage());
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onFailure(Call<CheckBarber> call, Throwable t) {
                            progress.setVisibility(View.GONE);
                            Log.e(TAG, "onFailureCheckPhone: " + t.getMessage());
                        }
                    });
        }
        */
    }

    private void goToHome(Barber barber) {
        Common.currentBarber = barber;
        Intent intent = new Intent(this, HomeStaffActivity.class);
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.btn_login), "transition_home");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {

        sService = Common.getBarbersAPI();
        settings = new Settings(this);
        progress.setVisibility(View.GONE);
    }

    private boolean validateUsername() {
        String username = txtInputUsername.getEditText().getText().toString().trim();
//        String checkspaces = "\\A\\w{1,20}\\z";

        if (TextUtils.isEmpty(username)) {
            txtInputUsername.setError(getString(R.string.please_enter_username_or_phone));
            return false;
        } else if (username.length() > 20) {
            txtInputUsername.setError(getString(R.string.username_large));
            return false;
        } else {
            txtInputUsername.setError(null);
            txtInputUsername.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {

        String password = txtInputPassword.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            txtInputPassword.setError(getString(R.string.please_enter_password));
            return false;
        } else if (password.length() < 6) {
            txtInputPassword.setError(getString(R.string.password_must_be_at_least_6));
            return false;
        } else {
            txtInputPassword.setError(null);
            txtInputPassword.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }
}