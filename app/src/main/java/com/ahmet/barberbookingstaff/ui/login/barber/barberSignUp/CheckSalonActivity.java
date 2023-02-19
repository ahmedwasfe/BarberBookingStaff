package com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.CheckSalon;
import com.ahmet.barberbookingstaff.ui.login.barber.barberLogin.LoginActivity;
import com.facebook.login.Login;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckSalonActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = CheckSalonActivity.class.getSimpleName();

    @BindView(R.id.text_create_account)
    TextView textCreateAccount;
    @BindView(R.id.text_numbers_of_screens)
    TextView textScreens;
    @BindView(R.id.btn_back_arrow)
    ImageView imgBack;
    @BindView(R.id.btn_check_salon)
    Button checkSalon;
    @BindView(R.id.btn_login)
    Button btnLogin;

    @BindView(R.id.txt_input_email)
    TextInputLayout txtInputEmail;
    @BindView(R.id.input_check_email)
    TextInputEditText inputCheckEmail;
    @BindView(R.id.relative_progress)
    RelativeLayout progress;

    private IBarbersAPI sService;
    private Settings settings;
    private String salonEmail;

    @OnClick(R.id.btn_check_salon)
    void onCheckSalonClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snack_error_layout, findViewById(R.id.checK_salon_container),
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }

        if (!vaildateEmail())
            return;

        String salonEmail = txtInputEmail.getEditText().getText().toString();

        checkSalon(salonEmail);

        Log.d(TAG, "onCheckSalonClick: " + salonEmail);
    }

    @OnClick(R.id.btn_login)
    void onLoginClick(){
        Intent intent = new Intent(this, LoginActivity.class);
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.btn_login), "transition_login");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);

    }

    @OnClick(R.id.btn_back_arrow)
    void onBackClick(){
        onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_salon);
        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {

        sService = Common.getBarbersAPI();
        settings = new Settings(this);
        progress.setVisibility(View.GONE);
    }

    private boolean vaildateEmail(){
        String email = txtInputEmail.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            txtInputEmail.setError(getString(R.string.please_enter_email_or_salon_name));
            return false;
        }else if(!Common.isEmailValid(email)){
            txtInputEmail.setError(getString(R.string.this_email_not_valid));
            return false;
        }else{
            txtInputEmail.setError(null);
            txtInputEmail.setErrorEnabled(false);
            return true;
        }
    }

    private void checkSalon(String text) {

        progress.setVisibility(View.VISIBLE);

        sService.getSalonByNameOrEmail(text, text)
                .enqueue(new Callback<CheckSalon>() {
                    @Override
                    public void onResponse(Call<CheckSalon> call, Response<CheckSalon> response) {
                        if (response.body().isExists()){
                            goToNextStep(response.body().getEmail());
                            progress.setVisibility(View.GONE);
                            Log.d(TAG, "onResponse: " + response.body().getEmail());
                        }else{
                            progress.setVisibility(View.GONE);
                            Toast.makeText(CheckSalonActivity.this, "This salon no exists", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckSalon> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });


    }

    private void goToNextStep(String email) {

        Intent intent = new Intent(CheckSalonActivity.this, BarberDataActivity.class);
        intent.putExtra(Common.KEY_EMAIL_SIGNUP, email);
        Pair[] pairs = new Pair[5];
        pairs[0] = new Pair<View, String>(imgBack, "transition_back_arrow");
        pairs[1] = new Pair<View, String>(checkSalon, "transition_create_account");
        pairs[2] = new Pair<View, String>(btnLogin, "transition_login_btn");
        pairs[3] = new Pair<View, String>(textCreateAccount, "transition_title_text");
        pairs[4] = new Pair<View, String>(textScreens, "transition_screen_number");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        } else
            startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }
}
