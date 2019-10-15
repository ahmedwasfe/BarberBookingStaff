package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class AddServiceFragmnet extends Fragment {

    private Unbinder mUnbinder;

    @BindView(R.id.txt_input_service_name)
    TextInputEditText mInputServiceName;
    @BindView(R.id.txt_input_service_price)
    TextInputEditText mInputServicePrice;

    @OnClick(R.id.btn_add_service)
    void submitService(){

        String serviceName = mInputServiceName.getText().toString();
        int servicePrice = Integer.parseInt(mInputServicePrice.getText().toString());

        addService(serviceName, servicePrice);
    }

    private AlertDialog mDialog;

    private void addService(String serviceName, int servicePrice) {

        mDialog.show();

        Map<String, Object> mMapService = new HashMap<>();
        mMapService.put("serviceName", serviceName);
        mMapService.put("servicePrice", servicePrice);

        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberID())
                .collection("Services")
                .add(mMapService)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                        if (task.isSuccessful()){
                            mDialog.dismiss();
                            Toast.makeText(getActivity(), "Addedd Services Successfull", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private static AddServiceFragmnet instance;
    public static AddServiceFragmnet getInstance(){
        if (instance == null)
            instance = new AddServiceFragmnet();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_service, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();

        return layoutView;
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setCancelable(false)
                .setMessage("Please wait...")
                .build();
    }
}