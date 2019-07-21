package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.Shopping;

import java.util.List;

public interface IShoppingLoadListener {

    void onShoppingLoadSuccess(List<Shopping> mListShopping);
    void onShoppingLoadFailed(String error);
}
