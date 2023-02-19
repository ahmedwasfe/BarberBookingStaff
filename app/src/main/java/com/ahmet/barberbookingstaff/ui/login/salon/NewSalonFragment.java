package com.ahmet.barberbookingstaff.ui.login.salon;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.Settings;
import com.ahmet.barberbookingstaff.model.CheckSalon;
import com.ahmet.barberbookingstaff.model.Salon;
import com.ahmet.barberbookingstaff.ui.login.barber.barberSignUp.BarberDataActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewSalonFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = NewSalonFragment.class.getSimpleName();

    @BindView(R.id.containter)
    ScrollView containter;
    @BindView(R.id.txt_input_salon_name)
    TextInputLayout txtInputSalonName;
    @BindView(R.id.txt_input_email)
    TextInputLayout txtInputEmail;
    @BindView(R.id.txt_input_phone)
    TextInputLayout txtInputPhone;
    @BindView(R.id.ccp)
    CountryCodePicker ccp;
    @BindView(R.id.txt_input_city)
    TextInputLayout txtInputCity;
    @BindView(R.id.spinner_salon_type)
    MaterialSpinner spinnerSalonType;
    @BindView(R.id.relative_progress)
    RelativeLayout loading;
    @BindView(R.id.btn_new_salon)
    Button btnNewSalon;

    private IBarbersAPI sService;
    private String salonType;
    private static String email;

    private FusedLocationProviderClient sFusedClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location sCurrenLocation;

    private AlertDialog dialog;

    private Settings settings;

    @OnClick(R.id.btn_new_salon)
    void onNewSalonClick() {

        if (!settings.isConnected()) {
            Common.showSnackBar(getActivity(), R.layout.snack_error_layout, containter,
                    getString(R.string.check_internet_connection), Snackbar.LENGTH_LONG, this);
            return;
        }

        String name = txtInputSalonName.getEditText().getText().toString();
        String email = txtInputEmail.getEditText().getText().toString();
        String phoneNumber = txtInputPhone.getEditText().getText().toString();
        String phoneCode = ccp.getSelectedCountryCode();
        String city = txtInputCity.getEditText().getText().toString();
        String _phoneNumber = "+" + phoneCode + phoneNumber;

        if (!validateSalonName(name) | !validateEmail(email) | !validatePhone(phoneNumber) |
                !validateCity(city) | !validateSalonType(salonType))
            return;

        Log.d(TAG, "onNewSalonClick: " + sCurrenLocation.getLatitude() + " : " + sCurrenLocation.getLongitude());
        createNewSalon(name, email, _phoneNumber, city,
                salonType, sCurrenLocation.getLatitude(), sCurrenLocation.getLongitude());
    }

    public NewSalonFragment(String email) {
        this.email = email;
    }

    private static NewSalonFragment instance;

    public static NewSalonFragment getInstance(String email) {
        return instance == null ? new NewSalonFragment(email) : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_new_salon, container, false);
        ButterKnife.bind(this, layoutView);
        initUI();
        getCurrentLocation();
        buildLocationReuest();
        loadSalonType();

        return layoutView;
    }

    private void initUI() {

        settings = new Settings(getActivity());
        sFusedClient = LocationServices.getFusedLocationProviderClient(getActivity());

        loading.setVisibility(View.GONE);
        txtInputEmail.getEditText().setText(email);
        txtInputEmail.setEnabled(false);

        dialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(getActivity())
                .setMessage(getString(R.string.create_account))
                .build();

        sService = Common.getBarbersAPI();

        spinnerSalonType.setOnItemSelectedListener((view, position, id, item) -> {
            salonType = item.toString();
        });
    }

    private void createNewSalon(String name, String email, String phoneNumber, String city, String salonType, double latitude, double longitude) {
        loading.setVisibility(View.VISIBLE);
        sService.checkSalonExists(email)
                .enqueue(new Callback<CheckSalon>() {
                    @Override
                    public void onResponse(Call<CheckSalon> call, Response<CheckSalon> response) {
                        if (response.isSuccessful()) {
                            if (!response.body().isExists()) {
                                sService.createNewSalon(email, name, phoneNumber, city, salonType, latitude, longitude, 1)
                                        .enqueue(new Callback<Salon>() {
                                            @Override
                                            public void onResponse(Call<Salon> call, Response<Salon> response) {
                                                if (response.isSuccessful()) {
                                                    loading.setVisibility(View.GONE);
                                                    Log.d(TAG, "onResponse: " + new Gson().toJson(response.body()));
                                                    Common.showSnackBar(getActivity(), R.layout.snack_success_layout,
                                                            containter, getString(R.string.create_salon_success));
                                                    goToNewBarber(response.body().getEmail());
                                                } else {
                                                    loading.setVisibility(View.GONE);
                                                    Common.showSnackBar(getActivity(), R.layout.snack_error_layout, containter, response.message());
                                                    Log.e(TAG, "onResponse: " + response.message() + " : " + response.errorBody());
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Salon> call, Throwable t) {
                                                loading.setVisibility(View.GONE);
                                                Log.e(TAG, "onFailure: " + t.getMessage());
                                                Common.showSnackBar(getActivity(), R.layout.snack_error_layout, containter, t.getMessage());
                                            }
                                        });
                            } else {
                                loading.setVisibility(View.GONE);
                                goToNewBarber(email);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckSalon> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });


    }

    private void goToNewBarber(String email) {

        Intent intent = new Intent(getActivity(), BarberDataActivity.class);
        intent.putExtra(Common.KEY_EMAIL_SIGNUP, email);

        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(btnNewSalon, "transition_create_account");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
            startActivity(intent, options.toBundle());
        }else
            startActivity(intent);
    }

    private void loadSalonType() {
        List<String> listSalonType = new ArrayList<>();
        listSalonType.add(getString(R.string.please_select_salon_type));
        listSalonType.add(getString(R.string.men));
        listSalonType.add(getString(R.string.women));
        listSalonType.add(getString(R.string.both));
        spinnerSalonType.setItems(listSalonType);
    }

    private void buildLocationReuest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            sFusedClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sCurrenLocation = task.getResult();
                            if (sCurrenLocation != null) {

                            } else {
                                buildLocationReuest();
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null)
                                            return;
                                        sCurrenLocation = locationResult.getLastLocation();
                                        sFusedClient.removeLocationUpdates(locationCallback);
                                    }
                                };

                                sFusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                            }
                        }
                    }).addOnFailureListener(e -> {
                Log.e(TAG, "getCurrentLocation: " + e.getMessage());
            });
        } else {
            showDialogRequestpermission();
        }
    }

    private void showDialogRequestpermission() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.permission)
                .setMessage(R.string.permission_to_access)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, Common.PERMISSION_REQUEST_CODE);
//                        Intent intent = new Intent();
//                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
//                        startActivity(intent);
                    }
                }).show();
    }

    private boolean validateSalonName(String salonName) {

        if (TextUtils.isEmpty(salonName)) {
            txtInputSalonName.setError(getString(R.string.please_enter_salon_name));
            return false;
        } else {
            txtInputSalonName.setError(null);
            txtInputSalonName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail(String email) {

        if (TextUtils.isEmpty(email)) {
            txtInputEmail.setError(getString(R.string.please_enter_email_or_salon_name));
            return false;
        } else if (!Common.isEmailValid(email)) {
            txtInputEmail.setError(getString(R.string.this_email_not_valid));
            return false;
        } else {
            txtInputEmail.setError(null);
            txtInputEmail.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePhone(String phone) {

        if (TextUtils.isEmpty(phone)) {
            txtInputPhone.setError(getString(R.string.please_enter_phone_number));
            return false;
        } else {
            txtInputPhone.setError(null);
            txtInputPhone.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateCity(String city) {

        if (TextUtils.isEmpty(city)) {
            txtInputCity.setError(getString(R.string.please_enter_city));
            return false;
        } else {
            txtInputCity.setError(null);
            txtInputCity.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateSalonType(String salonType) {

        if (TextUtils.isEmpty(salonType)) {
            Toast.makeText(getActivity(), getString(R.string.please_select_salon_type), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }
}
