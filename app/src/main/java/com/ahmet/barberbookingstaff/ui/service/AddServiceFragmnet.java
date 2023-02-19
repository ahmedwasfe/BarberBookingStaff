package com.ahmet.barberbookingstaff.ui.service;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.model.Services;
import com.google.firebase.database.FirebaseDatabase;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddServiceFragmnet extends Fragment {


    @BindView(R.id.input_service_name)
    EditText mInputServiceName;
    @BindView(R.id.input_service_price)
    EditText mInputServicePrice;

    @BindView(R.id.progress_add_service)
    RotateLoading rotateLoading;

    @OnClick(R.id.btn_add_service)
    void submitService(){

        String serviceName = mInputServiceName.getText().toString();
        String servicePrice = mInputServicePrice.getText().toString();

        Services service = new Services();
        service.setServiceName(serviceName);
        service.setServicePrice(TextUtils.isEmpty(servicePrice) ? 0 : Long.parseLong(servicePrice));
        addService(service);
    }

    private void addService(Services service) {

        rotateLoading.start();

        String id = UUID.randomUUID().toString();
        service.setId(id);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_SERICES_REFERANCE)
                .child(id)
                .setValue(service)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        rotateLoading.stop();
                        Toast.makeText(getActivity(), getString(R.string.add_service_success), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                   rotateLoading.stop();
                   Common.setFragment(ServicesFragment.getInstance(), R.id.frame_layout_home,
                           getActivity().getSupportFragmentManager());
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private static AddServiceFragmnet instance;
    public static AddServiceFragmnet getInstance(){
        return instance == null ? new AddServiceFragmnet() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_service, container, false);

        ButterKnife.bind(this, layoutView);
        rotateLoading.stop();

        return layoutView;
    }


}