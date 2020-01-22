package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Model.EventBus.BarberEvent;
import com.ahmet.barberbookingstaff.Model.EventBus.EnableNextButton;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class AddSalonFragment extends Fragment {


    @BindView(R.id.input_salon_name)
    EditText mInputSalonName;
    @BindView(R.id.input_salon_city)
    EditText mInputSalonCity;
    @BindView(R.id.input_salon_address)
    EditText mInputSalonAddress;
    @BindView(R.id.input_salon_phone)
    EditText mInputSalonPhone;
    @BindView(R.id.input_salon_open_hour)
    EditText mInputSalonOpenHour;
    @BindView(R.id.spinner_salon_type)
    MaterialSpinner mSpinnerSalonType;

    private FirebaseAuth mAuth;


    @OnClick(R.id.btn_add_salon)
    void btnAddSalon() {

        String salonName = mInputSalonName.getText().toString();
        String salonCity = mInputSalonCity.getText().toString();
        String salonAddress = mInputSalonAddress.getText().toString();
        String salonPhone = mInputSalonPhone.getText().toString();
        String openHour = mInputSalonOpenHour.getText().toString();

        if (TextUtils.isEmpty(salonName)){
            mInputSalonName.setError(getString(R.string.please_enter_salon_name));
            return;
        }

        if (TextUtils.isEmpty(salonCity)){
            mInputSalonCity.setError(getString(R.string.please_enter_salon_city));
            return;
        }

        if (TextUtils.isEmpty(salonAddress)){
            mInputSalonAddress.setError(getString(R.string.please_enter_salon_address));
            return;
        }

        if (TextUtils.isEmpty(mSalonType))
            Common.showSnackBar(getActivity(), mSpinnerSalonType, getString(R.string.please_select_salon_type));
        else
            verifySalon(salonName, salonCity, salonAddress, salonPhone, openHour);
    }

    private String mSalonType = "";

    private AlertDialog mDialog;


    private static AddSalonFragment instance;

    public static AddSalonFragment getInstance() {

        return instance == null ? new AddSalonFragment() : instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_salon, container, false);

        ButterKnife.bind(this, layoutView);

        mSpinnerSalonType.setOnItemSelectedListener((view, position, id, item) -> {
            mSalonType = item.toString();
            // Toast.makeText(getContext(), mSalonType, Toast.LENGTH_SHORT).show();
        });


        init();
        selectSalonType();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            EventBus.getDefault().postSticky(new EnableNextButton(2, Common.currentSalon));

        return layoutView;
    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(getActivity())
                .setMessage(R.string.please_wait)
                .build();
        ;
    }

    private void verifySalon(String salonName, String salonCity, String salonAddress, String salonPhone, String openHour) {

        mDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(user.getEmail())
                .get()
                .addOnCompleteListener(task1 -> {

                    if (task1.isSuccessful()) {
                        DocumentSnapshot snapshot = task1.getResult();
                        if (snapshot.exists()) {
                            mDialog.dismiss();
                            addSalon(salonName, user.getEmail(), salonAddress, salonPhone, openHour, mSalonType, salonCity);
                            // getCurrentLocationForSalon(currentSalonEmail);

                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.this_user_exists), Toast.LENGTH_SHORT).show();
                            Salon salon = new Salon();
                            salon.setSalonID(user.getEmail());
                            Toast.makeText(getActivity(), salon.getSalonID(), Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new EnableNextButton(2, Common.currentSalon));

                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(),
                        e.getMessage(), Toast.LENGTH_SHORT).show());
        }

    }

    private void addSalon(String salonName, String email, String salonAddress,
                          String salonPhone, String openHour, String mSalonType,
                          String salonCity) {


        Salon salon = new Salon(salonName, email, salonAddress,
                "", salonPhone, openHour, mSalonType, email, salonCity, true);

        Map<String, Object> mMapAddSAlon = new HashMap<>();
        mMapAddSAlon.put("name", salonName);
        mMapAddSAlon.put("address", salonAddress);
        mMapAddSAlon.put("phone", salonPhone);
        mMapAddSAlon.put("openHour", openHour);
        mMapAddSAlon.put("salonType", mSalonType);
        mMapAddSAlon.put("city", salonCity);
        mMapAddSAlon.put("open", true);

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(email)
                .update(mMapAddSAlon)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mDialog.dismiss();
                        Toast.makeText(getActivity(), getString(R.string.add_salon_success), Toast.LENGTH_SHORT).show();
                        salon.setSalonID(email);
                        EventBus.getDefault().postSticky(new EnableNextButton(2, Common.currentSalon));
                    }
                }).addOnFailureListener(e -> {
            mDialog.dismiss();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }


    private void selectSalonType() {

        List<String> mListSalonType = new ArrayList<>();
        mListSalonType.add(getString(R.string.select_salon_type));
        mListSalonType.add(getString(R.string.men));
        mListSalonType.add(getString(R.string.women));
        mListSalonType.add(getString(R.string.both));

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mListSalonType);
        mSpinnerSalonType.setAdapter(adapter);
    }

}