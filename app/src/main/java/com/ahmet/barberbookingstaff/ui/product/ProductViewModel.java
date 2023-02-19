package com.ahmet.barberbookingstaff.ui.product;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ahmet.barberbookingstaff.callback.IProductsLoadListener;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Products;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductViewModel extends ViewModel implements IProductsLoadListener {

    private MutableLiveData<List<Products>> listMutableProducts;
    private MutableLiveData<String> messageError;

    private IProductsLoadListener productsLoadListener;

    public ProductViewModel() {
        if (listMutableProducts == null){
            listMutableProducts = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
        }
        productsLoadListener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<Products>> getListMutableProducts() {
        loadAllProduct();
        return listMutableProducts;
    }

    public void loadAllProduct() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_PRODUCTS_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            if (dataSnapshot != null) {
                                List<Products> productsList = new ArrayList<>();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Products products = snapshot.getValue(Products.class);
                                    products.setId(snapshot.getKey());
                                    productsList.add(products);
                                }

                                productsLoadListener.onShoppingLoadSuccess(productsList);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        productsLoadListener.onShoppingLoadFailed(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onShoppingLoadSuccess(List<Products> mListProducts) {
        listMutableProducts.setValue(mListProducts);
    }

    @Override
    public void onShoppingLoadFailed(String error) {
        messageError.setValue(error);
    }
}
