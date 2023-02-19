package com.ahmet.barberbookingstaff.ui.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ahmet.barberbookingstaff.callback.IServicesListener;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Services;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ServicesViewModel extends ViewModel implements IServicesListener {

    private MutableLiveData<List<Services>> listMutableService;
    private MutableLiveData<String> mutableMessageError;

    private IServicesListener servicesListener;

    public ServicesViewModel() {

        if (listMutableService == null){
            listMutableService = new MutableLiveData<>();
            mutableMessageError = new MutableLiveData<>();
        }

        servicesListener = this;
    }

    public MutableLiveData<String> getMutableMessageError() {
        return mutableMessageError;
    }

    public MutableLiveData<List<Services>> getListMutableService() {
        loadAllServices();
        return listMutableService;
    }

    public void loadAllServices() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_SERICES_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            if (dataSnapshot != null){

                                List<Services> servicesList = new ArrayList<>();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    Services services = snapshot.getValue(Services.class);
                                    services.setId(snapshot.getKey());
                                    servicesList.add(services);
                                }
                                servicesListener.onLoadServicesSuccess(servicesList);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        servicesListener.onLoadServicesFailed(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadServicesSuccess(List<Services> mListServices) {
        listMutableService.setValue(mListServices);
    }

    @Override
    public void onLoadServicesFailed(String error) {
        mutableMessageError.setValue(error);
    }
}
