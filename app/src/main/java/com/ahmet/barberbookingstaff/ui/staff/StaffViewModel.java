package com.ahmet.barberbookingstaff.ui.staff;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ahmet.barberbookingstaff.callback.IStaffLoadCallbackListener;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Barber;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StaffViewModel extends ViewModel implements IStaffLoadCallbackListener {

    private MutableLiveData<List<Barber>> listMutableBarber;
    private MutableLiveData<String> mutableMessageError;

    private IStaffLoadCallbackListener staffLoadCallbackListener;

    public StaffViewModel() {

        if (listMutableBarber == null) {
            listMutableBarber = new MutableLiveData<>();
            mutableMessageError = new MutableLiveData<>();
        }

        staffLoadCallbackListener = this;
    }

    public MutableLiveData<String> getMutableMessageError() {
        return mutableMessageError;
    }

    public MutableLiveData<List<Barber>> getListMutableBarber() {
        loadAllStaff();
        return listMutableBarber;
    }

    public void loadAllStaff() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            List<Barber> listBarber = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Barber barber = snapshot.getValue(Barber.class);
                                barber.setBarberId(snapshot.getKey());
                                listBarber.add(barber);
                            }
                            staffLoadCallbackListener.onLoadStaffSuccess(listBarber);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        staffLoadCallbackListener.onLoadStaffFailed(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadStaffSuccess(List<Barber> mListStaff) {
        listMutableBarber.setValue(mListStaff);
    }

    @Override
    public void onLoadStaffFailed(String error) {
        mutableMessageError.setValue(error);
    }
}
