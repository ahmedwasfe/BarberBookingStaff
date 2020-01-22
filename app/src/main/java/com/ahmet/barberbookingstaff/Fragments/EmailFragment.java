package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Model.EventBus.EnableNextButton;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class EmailFragment extends Fragment {


    @BindView(R.id.input_email)
    EditText mInputEmail;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private PlacesClient mPlaceClient;
    private Location mLastLocation;

    private FirebaseAuth mAuth;

    private AlertDialog mDialog;

    @OnClick(R.id.btn_add_email)
    void btnAddEmail(){

        String email = mInputEmail.getText().toString();



        if (TextUtils.isEmpty(email)){
            mInputEmail.setError(getString(R.string.please_enter_email));
            return;
        }

        if (!Common.isEmailValid(email)){
            mInputEmail.setError(getString(R.string.email_pattern));
            return;
        }

        verifySalon(email);


    }

    private static EmailFragment instance;
    public static EmailFragment getInstance(){

        return instance == null ? new EmailFragment() : instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layouView = inflater.inflate(R.layout.fragment_email, container, false);

        ButterKnife.bind(this, layouView);

        init();

        return layouView;
    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        Places.initialize(getActivity(), getString(R.string.google_api_key));
        mPlaceClient = Places.createClient(getActivity());

        mDialog = new SpotsDialog.Builder()
                .setMessage(R.string.please_wait)
                .setContext(getActivity())
                .setCancelable(false)
                .build();
    }

    private void verifySalon(String currentSalonEmail) {

        mDialog.show();

        mAuth.createUserWithEmailAndPassword(currentSalonEmail, "saja2002")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            task.getResult().getUser().sendEmailVerification();

                            FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                                    .document(currentSalonEmail)
                                    .get()
                                    .addOnCompleteListener(task1 -> {

                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot snapshot = task1.getResult();
                                            if (!snapshot.exists()) {
                                                mDialog.dismiss();
                                                addEmail(currentSalonEmail);
                                                getCurrentLocationForSalon(currentSalonEmail);

                                            } else {
                                                mDialog.dismiss();
                                                Toast.makeText(getActivity(), getString(R.string.this_user_exists), Toast.LENGTH_SHORT).show();
                                                Salon salon = new Salon();
                                                salon.setSalonID(currentSalonEmail);
                                                Toast.makeText(getActivity(), salon.getSalonID(), Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new EnableNextButton(1, Common.currentSalon));

                                            }
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getActivity(),
                                            e.getMessage(), Toast.LENGTH_SHORT).show());
                        }

                    }
                }).addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().postSticky(new EnableNextButton(1, Common.currentSalon));
                });



    }

    private void addEmail(String currentSalonEmail) {

        Salon salon = new Salon("",currentSalonEmail, "","","","","",currentSalonEmail,"", false);
        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(currentSalonEmail)
                .set(salon)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        mDialog.dismiss();
                        salon.setSalonID(currentSalonEmail);
                        Toast.makeText(getActivity(), getString(R.string.add_email_success), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getCurrentLocationForSalon(String currentSalonEmail) {

        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()){
                            mLastLocation = task.getResult();
                            if (mLastLocation != null){

                                Log.i("Current_Latitude", String.valueOf(mLastLocation.getLatitude()));
                                Log.i("Current_Longitude", String.valueOf(mLastLocation.getLongitude()));
                                addCurrentLocation(currentSalonEmail, mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            }else {
                                mLocationRequest = LocationRequest.create();
                                mLocationRequest.setInterval(10000);
                                mLocationRequest.setFastestInterval(5000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                mLocationCallback = new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);

                                        if (locationResult == null)
                                            return;

                                        mLastLocation = locationResult.getLastLocation();
                                        addCurrentLocation(currentSalonEmail, mLastLocation.getLatitude(), mLastLocation.getLongitude());

                                        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                                    }
                                };
                            }
                        }else {
                            Toast.makeText(getActivity(), getString(R.string.can_not_get_location), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addCurrentLocation(String email, double latitude, double longitude) {

        Salon salon = new Salon();
        Map<String, Object> mMapSalonLocation = new HashMap<>();
        mMapSalonLocation.put("latitude", latitude);
        mMapSalonLocation.put("longitude", longitude);

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(email)
                .update(mMapSalonLocation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), getString(R.string.add_location_success), Toast.LENGTH_SHORT).show();
                        salon.setSalonID(email);
                        EventBus.getDefault().postSticky(new EnableNextButton(1, Common.currentSalon));
                    }
                });
    }
}
