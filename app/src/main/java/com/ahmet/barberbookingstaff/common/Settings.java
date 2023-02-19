package com.ahmet.barberbookingstaff.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Settings {

    private Context context;

    public Settings(Context context) {
        this.context = context;
    }

    public boolean isConnected(){

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected()))
            return true;
        else
            return false;
    }
}
