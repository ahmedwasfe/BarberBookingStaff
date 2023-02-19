package com.ahmet.barberbookingstaff.ui.login.salon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.MainActivity;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.WelcomeActivity;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.CheckSalon;
import com.ahmet.barberbookingstaff.model.Salon;
import com.ahmet.barberbookingstaff.ui.login.barber.barberLogin.LoginActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewSalonActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = NewSalonActivity.class.getSimpleName();

    @BindView(R.id.img_app_logo)
    ImageView imgAppLogo;
    @BindView(R.id.text_app_name)
    TextView tvAppName;
    @BindView(R.id.text_create_your_salon)
    TextView tvCreateSalon;
    @BindView(R.id.ll_login_salon)
    LinearLayout llLoginSalon;
    @BindView(R.id.progress_loading_check_user)
    RotateLoading mLoadingCheckUser;

    private CallbackManager callbackManager;
    private IBarbersAPI sService;

    private BottomSheetDialog mSheetDialog;

    private String salonType = "";

    private List<AuthUI.IdpConfig> mListProviders;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private Location mLastLocation;

    private Settings settings;

    @OnClick(R.id.btn_back_arrow)
    void onBackNewsalonClick(){
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }

    @OnClick(R.id.btn_facebook_login)
    void onFacebookLoginClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snack_error_layout, findViewById(R.id.home_container),
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }

        signInWithFacebook();
    }

    @OnClick(R.id.btn_google_login)
    void onGoogleLoginClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(this, R.layout.snack_error_layout, findViewById(R.id.home_container),
                    "Please check internt connection and try again", Snackbar.LENGTH_LONG, this);
            return;
        }

        signInWithGoogle();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_salon);
        ButterKnife.bind(NewSalonActivity.this);

        initUI();
        initLocation();

//        searchView.addTextChangedListener(new TextInputWatcher());

    }

    private void initLocation() {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initUI() {

        mLoadingCheckUser.stop();

        sService = Common.getBarbersAPI();
        settings = new Settings(this);
        callbackManager = CallbackManager.Factory.create();
        mListProviders = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        mListProviders = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = firebaseAuth -> {


            Dexter.withContext(this)
                    .withPermissions(Arrays.asList(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION))
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {

//                                checkUserIsExsist(user);

                            } else {
//                                showUIPhonenumber();
                            }

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                        }
                    }).check();

        };


    }

    private void signInWithGoogle() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(mListProviders)
                .build(), Common.CODE_REQUEST_SIGNIN);
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "onError: " + error.getMessage());
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnFailureListener(e -> Log.e(TAG, "handleFacebookAccessToken: " + e.getMessage()))
                .addOnSuccessListener(authResult -> {
                    checkSalonExsist(authResult.getUser().getEmail());
                    Log.d(TAG, "handleFacebookAccessToken: " + authResult.getUser().getEmail() + " : " +
                            authResult.getUser().getDisplayName() + " : " +
                            authResult.getUser().getPhoneNumber());
                });

    }

    private void checkUserIsExsist(FirebaseUser user) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            Salon salon = dataSnapshot.getValue(Salon.class);
                            FirebaseDatabase.getInstance().getReference()
                                    .child(Common.KEY_AllSALON_REFERANCE)
                                    .child(user.getUid())
                                    .child(Common.KEY_BARBER_REFERANCE)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                mLoadingCheckUser.start();
                                                Barber barber = snapshot.getValue(Barber.class);
                                                goToBarberLogin(salon, barber);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        } else {
                            mLoadingCheckUser.stop();
                            showDialogNewSalon(user);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void showDialogNewSalon(FirebaseUser user) {

        // init dialog
        mSheetDialog = new BottomSheetDialog(this);
        mSheetDialog.setCancelable(false);
        mSheetDialog.setCanceledOnTouchOutside(false);

        View sheetView = getLayoutInflater().inflate(R.layout.layout_sheet_sign_up, null);

        Button mBtnUpdate = sheetView.findViewById(R.id.btn_register);

        // Salon
        EditText mInputSalonName = sheetView.findViewById(R.id.input_register_salon_name);
        EditText mInputSalonCity = sheetView.findViewById(R.id.input_register_salon_city);
        MaterialSpinner mSpinner = sheetView.findViewById(R.id.spinner_barber_type_register);
        // Barber
        EditText mInputBarberName = sheetView.findViewById(R.id.input_register_barber_name);
        EditText mInputUsername = sheetView.findViewById(R.id.input_register_barber_username);
        EditText mInputPassword = sheetView.findViewById(R.id.input_register_barber_password);

        RotateLoading progressBar = sheetView.findViewById(R.id.progress_register);

        selectSalonType(mSpinner);
        mSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            salonType = item.toString();
        });

        progressBar.setVisibility(View.GONE);

        mBtnUpdate.setOnClickListener(v -> {

            progressBar.setVisibility(View.VISIBLE);
            progressBar.start();


            // Salon
            String salonName = mInputSalonName.getText().toString();
            String salonCity = mInputSalonCity.getText().toString();
            // Barber
            String barberName = mInputBarberName.getText().toString();
            String username = mInputUsername.getText().toString();
            String password = mInputPassword.getText().toString();

            if (TextUtils.isEmpty(salonName)) {
                mInputSalonName.setError(getString(R.string.please_enter_your_name));
                progressBar.stop();
                return;
            }
            if (TextUtils.isEmpty(salonCity)) {
                mInputSalonCity.setError(getString(R.string.please_enter_city));
                progressBar.stop();
                return;
            }

            if (TextUtils.isEmpty(barberName)) {
                mInputBarberName.setError(getString(R.string.please_enter_barber_name));
                progressBar.stop();
                return;
            }

            if (TextUtils.isEmpty(username)) {
                mInputUsername.setError(getString(R.string.please_enter_username));
                progressBar.stop();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mInputPassword.setError(getString(R.string.please_enter_password));
                progressBar.stop();
                return;
            }

            if (password.length() < 8) {
                mInputPassword.setError(getString(R.string.password_must_be_at_least_6));
                progressBar.stop();
                return;
            }

            if (!TextUtils.isEmpty(salonType))

                getCurrentLocationForSalon(user, salonName, salonCity, salonType,
                        barberName, username, password, progressBar);
            else {
                progressBar.setVisibility(View.GONE);
                progressBar.stop();
                Toast.makeText(this, getString(R.string.please_select_salon_type), Toast.LENGTH_SHORT).show();
            }
        });

        mSheetDialog.setContentView(sheetView);
        mSheetDialog.show();
    }

    private void getCurrentLocationForSalon(FirebaseUser user, String salonName, String salonCity, String salonType, String barberName, String username, String password, RotateLoading progressBar) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()) {
                            mLastLocation = task.getResult();
                            if (mLastLocation != null) {

                                Log.i("Current_Latitude", String.valueOf(mLastLocation.getLatitude()));
                                Log.i("Current_Longitude", String.valueOf(mLastLocation.getLongitude()));
                                signUp(user, salonName, salonCity, salonType,
                                        barberName, username, password,
                                        mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                        progressBar);

                            } else {
                                mLocationRequest = LocationRequest.create();
                                mLocationRequest.setInterval(10000);
                                mLocationRequest.setFastestInterval(5000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                mLocationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);

                                        if (locationResult == null)
                                            return;

                                        mLastLocation = locationResult.getLastLocation();
                                        signUp(user, salonName, salonCity, salonType,
                                                barberName, username, password,
                                                mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                                progressBar);

                                        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                                    }
                                };
                            }
                        } else {
                            Toast.makeText(NewSalonActivity.this, getString(R.string.can_not_get_location), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUp(FirebaseUser firebaseUser, String name, String city, String salonType,
                        String barberName, String username, String password, double latitude, double longitude, RotateLoading progressBar) {

//        Salon salon = new Salon(firebaseUser.getUid(), name, firebaseUser.getPhoneNumber(),"", city,
//                "", "", "", salonType,
//                latitude, longitude, false);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(firebaseUser.getUid())
                .setValue(null)
                .addOnFailureListener(e -> {
                    progressBar.stop();
                    progressBar.setVisibility(View.GONE);
                    Log.e("ERROR_ADD_SALON", e.getMessage());
                }).addOnCompleteListener(task -> {
            progressBar.stop();
            progressBar.setVisibility(View.GONE);
            checkBarberExsist(firebaseUser, null, barberName, username, password, progressBar);
        });

    }

    private void checkBarberExsist(FirebaseUser user, Salon salon, String name, String username, String password, RotateLoading progressBar) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(user.getUid())
                .child(Common.KEY_BARBER_REFERANCE)
                .orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            Barber barber = dataSnapshot.getValue(Barber.class);
                            goToBarberLogin(salon, barber);
                            Toast.makeText(NewSalonActivity.this, "Username is not available", Toast.LENGTH_SHORT).show();
                        } else
                            progressBar.stop();
                        registerNewBarber(user, salon, name, username, password, "Admin", progressBar);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void registerNewBarber(FirebaseUser user, Salon salon, String name, String username, String password, String barberType, RotateLoading progressBar) {

        String barberId = UUID.randomUUID().toString();

        Barber barber = new Barber(barberId, name, username, password, Common.KEY_IMAGE_DEFAULT, barberType, 0);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(user.getUid())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(barberId)
                .setValue(barber)
                .addOnFailureListener(e -> {
                    progressBar.stop();
                    Log.e("ERROR_ADD_BARBER", e.getMessage());
                }).addOnCompleteListener(task -> {
            progressBar.start();
            goToBarberLogin(salon, barber);

        });
    }


    private void goToBarberLogin(Salon salon, Barber barber) {

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Common.currentSalon = salon;
                    Common.currentBarber = barber;
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }).addOnCompleteListener(task -> {
            Common.currentBarber = barber;
            Common.updateToken(this, task.getResult().getToken());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void showUIPhonenumber() {

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(mListProviders)
                .build(), Common.CODE_REQUEST_SIGNIN
        );
    }

    private void selectSalonType(MaterialSpinner materialSpinner) {

        List<String> mListSalonType = new ArrayList<>();
        mListSalonType.add(getString(R.string.select_salon_type));
        mListSalonType.add(getString(R.string.men));
        mListSalonType.add(getString(R.string.women));
        mListSalonType.add(getString(R.string.both));

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mListSalonType);
        materialSpinner.setAdapter(adapter);
    }

    // ---------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.CODE_REQUEST_SIGNIN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Get User
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                checkSalonExsist(user.getEmail());
                Log.d(TAG, "onActivityResult: " + user.getEmail() + " : " +
                        user.getDisplayName() + " : " + user.getPhoneNumber());
            }
        }
    }

    private void checkSalonExsist(String email) {

        sService.checkSalonExists(email)
                .enqueue(new Callback<CheckSalon>() {
                    @Override
                    public void onResponse(Call<CheckSalon> call, Response<CheckSalon> response) {

                        if (response.isSuccessful()) {
                            if (response.body().isExists()) {
                                goToNewBarber(null, email);
                            } else {
                                llLoginSalon.setVisibility(View.GONE);
                                tvAppName.setVisibility(View.GONE);
                                tvCreateSalon.setVisibility(View.GONE);
                                imgAppLogo.setVisibility(View.GONE);
                                Common.setFragment(NewSalonFragment.getInstance(email),
                                        R.id.frame_layout_new_salon, getSupportFragmentManager());
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<CheckSalon> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    private void goToNewBarber(View view, String email) {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Common.KEY_EMAIL_SIGNUP, email);

        Pair[] pairs = new Pair[2];
        pairs[0] = new Pair<View, String>(findViewById(R.id.btn_facebook_login), "transition_login");
        pairs[1] = new Pair<View, String>(findViewById(R.id.btn_google_login), "transition_login");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        if (mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        super.onStop();
    }

    private void printKeyHash() {

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES
            );
            for (Signature signature : packageInfo.signatures) {
                MessageDigest digest = MessageDigest.getInstance("SHA");
                digest.update(signature.toByteArray());
                Log.d(TAG, "printKeyHash: " + Base64.encodeToString(digest.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }
}