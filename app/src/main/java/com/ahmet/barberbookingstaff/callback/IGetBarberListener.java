package com.ahmet.barberbookingstaff.callback;

import android.content.Context;

import com.ahmet.barberbookingstaff.model.Barber;

public interface IGetBarberListener {

    void onGetBarberSuccess(Context mContext, Barber barber);
}
