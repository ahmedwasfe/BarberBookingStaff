package com.ahmet.barberbookingstaff.callback;

import com.ahmet.barberbookingstaff.model.Barber;

import java.util.List;

public interface IStaffLoadCallbackListener {

    void onLoadStaffSuccess(List<Barber> mListStaff);
    void onLoadStaffFailed(String error);
}
