package com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.CheckBarber;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BarberDataActivity extends AppCompatActivity {
    private static final String TAG = Barber.class.getSimpleName();

    @BindView(R.id.text_create_account)
    TextView textCreateAccount;
    @BindView(R.id.text_numbers_of_screens)
    TextView textScreens;
    @BindView(R.id.btn_back_arrow)
    ImageView imgBack;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.relative_progress)
    RelativeLayout progress;
    @BindView(R.id.progress_check_username)
    ProgressBar progressCheckUser;
    @BindView(R.id.img_username_available)
    ImageView usernameAvailable;
    @BindView(R.id.tv_salon_name)
    TextView txtSalonName;

    @BindView(R.id.txt_input_full_name)
    TextInputLayout txtInputFullName;
    @BindView(R.id.txt_input_username)
    TextInputLayout txtInputUsername;
    @BindView(R.id.input_username)
    TextInputEditText inputUsername;
    @BindView(R.id.txt_input_password)
    TextInputLayout txtInputPassword;
    @BindView(R.id.txt_input_confirm_password)
    TextInputLayout txtInputConfirmPassword;
    @BindView(R.id.radio_gender)
    RadioGroup radioGender;
    @BindView(R.id.radio_male)
    RadioButton radioMale;
    @BindView(R.id.radio_female)
    RadioButton radioFemale;

    private int _gender;
    private String salonEmail;

    private IBarbersAPI sService;

    @OnClick(R.id.btn_next)
    void onNextClick() {

        if (!validateFullName() | !validateUsername() | !validatePassword() | !validateGender())
            return;


        Intent intent = new Intent(this, PhoneNumberActivity.class);
        intent.putExtra(Common.KEY_EMAIL_SIGNUP, salonEmail);
        intent.putExtra(Common.KEY_FULLNAME_SIGNUP, txtInputFullName.getEditText().getText().toString());
        intent.putExtra(Common.KEY_USERNAME_SIGNUP, txtInputUsername.getEditText().getText().toString());
        intent.putExtra(Common.KEY_PASSWORD_SIGNUP, txtInputPassword.getEditText().getText().toString());
        intent.putExtra(Common.KEY_GENDER_SIGNUP, _gender);

        Pair[] pairs = new Pair[5];
        pairs[0] = new Pair<View, String>(imgBack, "transition_back_arrow");
        pairs[1] = new Pair<View, String>(btnNext, "transition_create_account");
        pairs[2] = new Pair<View, String>(btnLogin, "transition_login_btn");
        pairs[3] = new Pair<View, String>(textCreateAccount, "transition_title_text");
        pairs[4] = new Pair<View, String>(textScreens, "transition_screen_number");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        } else
            startActivity(intent);
    }

    @OnClick(R.id.btn_back_arrow)
    void onBackClick(){
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_data);
        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {

        if (getIntent() != null)
            salonEmail = getIntent().getStringExtra(Common.KEY_EMAIL_SIGNUP);

        sService = Common.getBarbersAPI();
        Common.getSalonName(sService, txtSalonName, salonEmail);

        inputUsername.addTextChangedListener(new TextInputWatcher());

        progress.setVisibility(View.GONE);
        progressCheckUser.setVisibility(View.GONE);
        usernameAvailable.setVisibility(View.GONE);

        radioMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && radioMale != null)
                _gender = 0;
        });

        radioFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && radioFemale != null)
                _gender = 1;
        });


    }

    private boolean validateFullName(){
        String fullName = txtInputFullName.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(fullName)){
            txtInputFullName.setError(getString(R.string.please_enter_barber_name));
            return false;
        }else{
            txtInputFullName.setError(null);
            txtInputFullName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateUsername(){
        String username = txtInputUsername.getEditText().getText().toString().trim();
        String checkspaces = "\\A\\w{1,20}\\z";

        if (TextUtils.isEmpty(username)){
            txtInputUsername.setError(getString(R.string.please_enter_username));
            return false;
        }else if (username.length() > 20){
            txtInputUsername.setError(getString(R.string.username_large));
            return false;
        }else if (!username.matches(checkspaces)){
            txtInputUsername.setError(getString(R.string.no_spaces));
            return false;
        }else{
            txtInputUsername.setError(null);
            txtInputUsername.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword(){

        String password = txtInputPassword.getEditText().getText().toString().trim();
        String confirmPassword = txtInputConfirmPassword.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(password)){
            txtInputPassword.setError(getString(R.string.please_enter_password));
            return false;
        }else if (password.length() < 6){
            txtInputPassword.setError(getString(R.string.password_must_be_at_least_6));
            return false;
        }else if (TextUtils.isEmpty(confirmPassword)){
            txtInputConfirmPassword.setError(getString(R.string.please_enter_confirm_password));
            return false;
        }else if (confirmPassword.length() < 6){
            txtInputConfirmPassword.setError(getString(R.string.password_must_be_at_least_6));
            return false;
        }else if (!password.equals(confirmPassword)){
            txtInputConfirmPassword.setError(getString(R.string.the_two_passwords_do_not_match));
            return false;
        }else{
            txtInputPassword.setError(null);
            txtInputConfirmPassword.setError(null);
            txtInputPassword.setErrorEnabled(false);
            txtInputConfirmPassword.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateGender(){
        if (radioGender.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, getString(R.string.please_select_gender), Toast.LENGTH_SHORT).show();
            return false;
        }else
            return true;
    }

    private class TextInputWatcher implements TextWatcher {

        private Timer timer = new Timer();

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            progressCheckUser.setVisibility(View.VISIBLE);
            txtInputUsername.setError(null);
            txtInputUsername.setErrorEnabled(false);
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkUsername(text.toString());

                }
            }, 100);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void checkUsername(String username) {

        sService.checkBarberUsername(username)
                .enqueue(new Callback<CheckBarber>() {
                    @Override
                    public void onResponse(Call<CheckBarber> call, Response<CheckBarber> response) {

                        if (response.body().isExists()){
                            txtInputUsername.setError("this user not available");
                            progressCheckUser.setVisibility(View.GONE);
                            usernameAvailable.setVisibility(View.GONE);
                            return;
                        }else{
                            usernameAvailable.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckBarber> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }
}