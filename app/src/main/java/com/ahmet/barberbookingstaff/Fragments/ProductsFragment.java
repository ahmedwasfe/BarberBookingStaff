package com.ahmet.barberbookingstaff.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ahmet.barberbookingstaff.Adapter.ShoppingAdapter;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.Interface.IProductsLoadListener;
import com.ahmet.barberbookingstaff.Interface.IShoppingItemSelectedListener;
import com.ahmet.barberbookingstaff.Model.Products;
import com.ahmet.barberbookingstaff.R;
import com.github.ybq.android.spinkit.style.Circle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ProductsFragment extends BottomSheetDialogFragment
        implements IProductsLoadListener, IShoppingItemSelectedListener {

    private Unbinder mUnbinder;

    // firestore
    private CollectionReference mShoppingReference;

    private IShoppingItemSelectedListener callBackToActivity;
    private IProductsLoadListener mIProductsLoadListener;


    @BindView(R.id.recycler_shopping_items)
    RecyclerView mRecyclerShopping;


    private static ProductsFragment instance;

    public static ProductsFragment getInstance(IShoppingItemSelectedListener mIShoppingItemSelectedListener){
        return instance == null ? new ProductsFragment(mIShoppingItemSelectedListener) : instance;
    }


    private void loadShoppingItem(String itemMenu) {

        mShoppingReference = FirebaseFirestore.getInstance()
                .collection("Products")
                .document(itemMenu)
                .collection("Items");

        // Get data for shopping
        mShoppingReference
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIProductsLoadListener.onShoppingLoadFailed(e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){

                            List<Products> mListProducts = new ArrayList<>();

                            for (DocumentSnapshot documentSnapshot : task.getResult()){
                                Products products = documentSnapshot.toObject(Products.class);
                                // Remember add it if you dont want to get null referance
                                products.setId(documentSnapshot.getId());
                                mListProducts.add(products);
                                Log.d("SHOPPING_ITEM", "" + products.getName());
                            }
                            Log.d("SHOPPING_ITEM", "" + mListProducts.size());
                            mIProductsLoadListener.onShoppingLoadSuccess(mListProducts);
                        }
//                        else {
//                            Log.d("TASKEXCEPTION", task.getException().getMessage());
//                        }
                    }
                });
    }



    public ProductsFragment(IShoppingItemSelectedListener callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    public ProductsFragment() {}
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_shopping, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();
        initRecyclerView();

        loadShoppingItem("Wax");


        return layoutView;

    }

    private void init() {

        mIProductsLoadListener = this;
    }

    private void initRecyclerView() {

        mRecyclerShopping.setHasFixedSize(true);
        mRecyclerShopping.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        mRecyclerShopping.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onShoppingLoadSuccess(List<Products> mListProducts) {

        ShoppingAdapter mShoppingAdapter = new ShoppingAdapter(getActivity(), mListProducts, this);
        if (mShoppingAdapter != null)
            mRecyclerShopping.setAdapter(mShoppingAdapter);
        else {
            Toast.makeText(getActivity(), "Can,t load data in Adapter", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShoppingLoadFailed(String error) {

        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShoppingItemSelected(Products products) {

        callBackToActivity.onShoppingItemSelected(products);
    }
}
