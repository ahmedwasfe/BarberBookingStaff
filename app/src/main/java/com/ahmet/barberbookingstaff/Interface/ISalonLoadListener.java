package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.Salon;

import java.util.List;

public interface ISalonLoadListener {

    void onLoadAllSalonSuccess(List<Salon> mListSalon);
    void onLoadAllSalonFailed(String error);

}
