package com.ahmet.barberbookingstaff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.Login;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIMER = 5000;

    @BindView(R.id.img_app_logo)
    ImageView imgAppLogo;
    @BindView(R.id.text_app_name)
    TextView textAppName;
    @BindView(R.id.text_powered_by)
    TextView textPoweredBy;

    private Animation sideAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        sideAnim = AnimationUtils.loadAnimation(this, R.anim.side_anim);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);

        imgAppLogo.setAnimation(sideAnim);
        textAppName.setAnimation(sideAnim);
        textPoweredBy.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                finish();
            }
        }, SPLASH_TIMER);
    }
}