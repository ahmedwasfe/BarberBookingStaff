package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ahmet.barberbookingstaff.Adapter.ProductsAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Interface.IProductsLoadListener;
import com.ahmet.barberbookingstaff.Model.Products;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ShowProductFragment extends Fragment implements IProductsLoadListener {

    private Unbinder mUnbinder;

    @BindView(R.id.recycler_products)
    RecyclerView mRecyclerProducts;

    private AlertDialog mDialog;

    private IProductsLoadListener productsLoadListener;

    private static ShowProductFragment instance;
    public static ShowProductFragment getInstance(){

        if (instance == null)
            instance = new ShowProductFragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_show_products, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();

        loadAllProducts();

        return layoutView;
    }

    private void loadAllProducts() {

        mDialog.show();

        // /AllSalon/ahm.m@yahoo.com/Products/rLsRJgWCiBcVAFKtKqob
        // /AllSalon/currentSalonID/Products/rLsRJgWCiBcVAFKtKqob

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .collection(Common.KEY_COLLECTION_PRODUCTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        List<Products> mListProducts = new ArrayList<>();
                        for (DocumentSnapshot snapshot : task.getResult()){
                            Products products = snapshot.toObject(Products.class);
                            products.setId(snapshot.getId());
                            mListProducts.add(products);
                        }
                        productsLoadListener.onShoppingLoadSuccess(mListProducts);
                    }
                }).addOnFailureListener(e -> productsLoadListener.onShoppingLoadFailed(e.getMessage()));
    }

    private void init() {

        productsLoadListener = this;

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setCancelable(false)
                .setMessage(R.string.loading)
                .build();

        mRecyclerProducts.setHasFixedSize(true);
        mRecyclerProducts.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
    }

    @Override
    public void onShoppingLoadSuccess(List<Products> mListProducts) {

        ProductsAdapter productsAdapter = new ProductsAdapter(getActivity(), mListProducts);
        mRecyclerProducts.setAdapter(productsAdapter);

        mDialog.dismiss();
    }

    @Override
    public void onShoppingLoadFailed(String error) {

        mDialog.dismiss();
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }
}