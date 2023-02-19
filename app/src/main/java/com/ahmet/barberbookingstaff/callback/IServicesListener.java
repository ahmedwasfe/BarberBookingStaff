package com.ahmet.barberbookingstaff.callback;

import com.ahmet.barberbookingstaff.model.Services;

import java.util.List;

public interface IServicesListener {

    void onLoadServicesSuccess(List<Services> mListServices);
    void onLoadServicesFailed(String error);
}
