package com.ahmet.barberbookingstaff.ui.login.barber.forgotPassword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.ui.login.barber.barberLogin.LoginActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpdatePasswordSuccess extends AppCompatActivity {

    @BindView(R.id.update_password_countainer)
    LinearLayout layout;

    @OnClick(R.id.btn_login)
    void onLoginClick(){
        Intent intent = new Intent(this, LoginActivity.class);
        Pair[] pair = new Pair[1];
        pair[0] = new Pair<View, String>(findViewById(R.id.btn_login), "transition_login");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password_success);
        ButterKnife.bind(this);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);
        layout.setAnimation(animation);
    }
}