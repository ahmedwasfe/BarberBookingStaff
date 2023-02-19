package com.ahmet.barberbookingstaff.callback;

import com.ahmet.barberbookingstaff.model.Products;

import java.util.List;

public interface IProductsLoadListener {

    void onShoppingLoadSuccess(List<Products> mListProducts);
    void onShoppingLoadFailed(String error);
}
