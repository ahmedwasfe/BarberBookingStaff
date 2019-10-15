package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.Salon;

import java.util.List;

public interface IStaffLoadListener {

    void onLoadStaffSuccess(List<Barber> mListStaff);
    void onLoadStaffFailed(String error);
}
