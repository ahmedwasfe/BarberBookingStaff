package com.ahmet.barberbookingstaff.callback;

import com.ahmet.barberbookingstaff.model.Salon;

import java.util.List;

public interface ISalonLoadListener {

    void onLoadAllSalonSuccess(List<Salon> mListSalon);
    void onLoadAllSalonFailed(String error);

}
