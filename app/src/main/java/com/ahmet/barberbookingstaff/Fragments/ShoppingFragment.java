package com.ahmet.barberbookingstaff.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ahmet.barberbookingstaff.Adapter.ShoppingAdapter;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.DoneServicsesActivity;
import com.ahmet.barberbookingstaff.Interface.IShoppingItemSelectedListener;
import com.ahmet.barberbookingstaff.Interface.IShoppingLoadListener;
import com.ahmet.barberbookingstaff.Model.Shopping;
import com.ahmet.barberbookingstaff.R;
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

public class ShoppingFragment extends BottomSheetDialogFragment
        implements IShoppingLoadListener, IShoppingItemSelectedListener {

    private Unbinder mUnbinder;

    // firestore
    private CollectionReference mShoppingReference;

    private IShoppingItemSelectedListener callBackToActivity;
    private IShoppingLoadListener mIShoppingLoadListener;

    @BindView(R.id.chip_group)
    ChipGroup mChipGroup;


    @BindView(R.id.chip_wax)
    Chip chip_wax;
    @OnClick(R.id.chip_wax)
    void waxChipClick(){
        setSelecedChip(chip_wax);
        loadShoppingItem("Wax");

    }

    @BindView(R.id.chip_spray)
    Chip chip_spray;
    @OnClick(R.id.chip_spray)
    void sprayChipClick(){
        setSelecedChip(chip_spray);
        loadShoppingItem("Spray");
    }

    @BindView(R.id.chip_hair_care)
    Chip mChipHairCare;
    @OnClick(R.id.chip_hair_care)
    void chipHairCare(){
        setSelecedChip(mChipHairCare);
        loadShoppingItem("Hair Care");
    }

    @BindView(R.id.chip_body_care)
    Chip mChipBodyCare;
    @OnClick(R.id.chip_body_care)
    void chipBodyCare(){
        setSelecedChip(mChipBodyCare);
        loadShoppingItem("Body Care");
    }

    @BindView(R.id.recycler_shopping_items)
    RecyclerView mRecyclerShopping;

    private static ShoppingFragment instance;

    public static ShoppingFragment getInstance(IShoppingItemSelectedListener mIShoppingItemSelectedListener){
        return instance == null ? new ShoppingFragment(mIShoppingItemSelectedListener) : instance;
    }


    private void setSelecedChip(Chip chip) {

        // Set color
        for (int x = 0; x < mChipGroup.getChildCount(); x++){

            Chip mChipItem = (Chip) mChipGroup.getChildAt(x);
            // If not selected
            if (mChipItem.getId() != chip.getId()){
                mChipItem.setChipBackgroundColorResource(R.color.colorGray);
                mChipItem.setTextColor(getResources().getColor(R.color.colorBlack));

                // If selected
            } else {
                mChipItem.setChipBackgroundColorResource(R.color.colorPrimary);
                mChipItem.setTextColor(getResources().getColor(R.color.colorWhite));
            }
        }
    }

    private void loadShoppingItem(String itemMenu) {

        mShoppingReference = FirebaseFirestore.getInstance()
                .collection("Shopping")
                .document(itemMenu)
                .collection("Items");

        // Get data for shopping
        mShoppingReference
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIShoppingLoadListener.onShoppingLoadFailed(e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){

                            List<Shopping> mListShopping = new ArrayList<>();

                            for (DocumentSnapshot documentSnapshot : task.getResult()){
                                Shopping shopping = documentSnapshot.toObject(Shopping.class);
                                // Remember add it if you dont want to get null referance
                                shopping.setId(documentSnapshot.getId());
                                mListShopping.add(shopping);
                                Log.d("SHOPPING_ITEM", "" + shopping.getName());
                            }
                            Log.d("SHOPPING_ITEM", "" + mListShopping.size());
                            mIShoppingLoadListener.onShoppingLoadSuccess(mListShopping);
                        }
//                        else {
//                            Log.d("TASKEXCEPTION", task.getException().getMessage());
//                        }
                    }
                });
    }



    public ShoppingFragment(IShoppingItemSelectedListener callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    public ShoppingFragment() {}
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

        mIShoppingLoadListener = this;

    }

    private void initRecyclerView() {

        mRecyclerShopping.setHasFixedSize(true);
        mRecyclerShopping.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        mRecyclerShopping.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onShoppingLoadSuccess(List<Shopping> mListShopping) {

        ShoppingAdapter mShoppingAdapter = new ShoppingAdapter(getActivity(), mListShopping, this);
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
    public void onShoppingItemSelected(Shopping shopping) {

        callBackToActivity.onShoppingItemSelected(shopping);
    }
}
