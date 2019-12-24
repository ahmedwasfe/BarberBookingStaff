package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.SubActivity.AddSalonActivity;
import com.ahmet.barberbookingstaff.SubActivity.SalonActivity;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import butterknife.ButterKnife;

import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {


    Unbinder mUnbinder;

    @OnClick(R.id.btn_login_salon)
    void salonLogin(){
        startLoginPage(LoginType.EMAIL);
    }

    @OnClick(R.id.btn_login_barber)
    void barberLogin(){

        Intent intent = new Intent(HomeActivity.this, SalonActivity.class);
        intent.putExtra(Common.IS_LOGIN, false);
        startActivity(intent);
        finish();
    }
    private static final int CODE_REQUEST_FACEBOOK_KIT_LOGIN = 1000;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Add New Salon");


        Dexter.withActivity(this)
                .withPermissions(new String [] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {

                                if (task.isSuccessful()){

                                    Common.updateToken(HomeActivity.this,
                                            task.getResult().getToken());
                                    Log.d("TOKEN_HOME_ACTIVITY", task.getResult().getToken());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                Paper.init(HomeActivity.this);
                String user = Paper.book().read(Common.KEY_LOGGED);
                if (TextUtils.isEmpty(user)){

                    AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    if (accessToken != null){

                        Intent intent = new Intent(HomeActivity.this, SalonActivity.class);
                        intent.putExtra(Common.IS_LOGIN, false);
                        startActivity(intent);
                        finish();

                    }else {
                        // startLoginPage(LoginType.EMAIL);
                        setContentView(R.layout.activity_home);
                        mUnbinder = ButterKnife.bind(HomeActivity.this);
                        init();
                    }

                }else{
                    Gson gson = new Gson();
                    Common.currentSalon = gson.fromJson(Paper.book().read(Common.KEY_SALON,""),
                            new TypeToken<Salon>(){}.getType());
                    Common.currentBarber = gson.fromJson(Paper.book().read(Common.KEY_BARBER, ""),
                            new TypeToken<Barber>(){}.getType());

                    Intent intent = new Intent(HomeActivity.this, HomeStaffActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();


      }


    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(this)
                .build();

    }

    // ---------------------------------------------------------------------

    private void startLoginPage(LoginType loginType) {

        Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType,
                        AccountKitActivity.ResponseType.TOKEN);

        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, builder.build());
        startActivityForResult(intent, CODE_REQUEST_FACEBOOK_KIT_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_REQUEST_FACEBOOK_KIT_LOGIN){
            if (resultCode == RESULT_OK){

                AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
                if (loginResult.getError() != null){

                    // Toast.makeText(this, loginResult.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("TAG_LOGIN_RESULT", loginResult.getError().getErrorType().getMessage());

                }else if (loginResult.wasCancelled())

                    Log.e("TAG_LOGIN_RESULT", loginResult.getError().getErrorType().getMessage());

                else{
                    if (loginResult.getAccessToken() != null){
                        mDialog.show();
                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {

                                Intent intent = new Intent(HomeActivity.this, AddSalonActivity.class);
                                intent.putExtra(Common.IS_LOGIN, false);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {

                            }
                        });
                    }
                }
            }
        }
    }
}
