package com.ahmet.barberbookingstaff;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import com.ahmet.barberbookingstaff.ui.login.barber.barberLogin.LoginActivity;
import com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp.CheckSalonActivity;
import com.ahmet.barberbookingstaff.ui.login.salon.NewSalonActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeActivity extends AppCompatActivity {

    @OnClick(R.id.btn_go_to_login)
    void onGoToLoginClick(){
        Intent intent = new Intent(this, LoginActivity.class);
        Pair[] pair = new Pair[1];
        pair[0] = new Pair<View, String>(findViewById(R.id.btn_go_to_login), "transition_login");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    @OnClick(R.id.btn_go_to_signup)
    void onGoToSignUpClick(){

        Intent intent = new Intent(this, CheckSalonActivity.class);
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.btn_go_to_signup), "transition_create_account");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    @OnClick(R.id.btn_go_to_new_salon)
    void onGoToNewSalonClick(){
        Intent intent = new Intent(this, NewSalonActivity.class);
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.btn_go_to_new_salon), "transition_new_salon");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

    }

}