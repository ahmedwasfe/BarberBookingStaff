package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.BarberServices;

import java.util.List;

public interface IBarberServicesListener {

    void onLoadBarberServicesSuccess(List<BarberServices> mListBarberServices);
    void onLoadBarberServicesFailed(String error);
}
