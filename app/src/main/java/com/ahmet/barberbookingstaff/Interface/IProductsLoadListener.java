package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.Products;

import java.util.List;

public interface IProductsLoadListener {

    void onShoppingLoadSuccess(List<Products> mListProducts);
    void onShoppingLoadFailed(String error);
}
