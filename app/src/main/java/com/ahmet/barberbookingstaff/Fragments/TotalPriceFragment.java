package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.Adapter.ShoppingConfirmAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Model.BarberServices;
import com.ahmet.barberbookingstaff.Model.Shopping;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.Retrofit.IFCMService;
import com.ahmet.barberbookingstaff.Retrofit.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class TotalPriceFragment extends BottomSheetDialogFragment {

    private Unbinder mUnbinder;

    @BindView(R.id.text_salon_name)
    TextView mTxtSalonName;
    @BindView(R.id.text_barber_name)
    TextView mTxtBarberName;
    @BindView(R.id.text_customer_name)
    TextView mTxtCustomerName;
    @BindView(R.id.text_customer_phone)
    TextView mTxtCustomerPhone;
    @BindView(R.id.text_time_bookin)
    TextView mTxtTimeBooking;
    @BindView(R.id.txt_total_price)
    TextView mTxtTotalPrice;
    @BindView(R.id.chip_group_services)
    ChipGroup mChipGroup_Services;
    @BindView(R.id.recycler_items_shopping)
    RecyclerView mRecyclerShoopingItem;
    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    private HashSet<BarberServices> mHashServicesAdded;
    private List<Shopping> mListShopping;

    private IFCMService mIFCMService;

    private AlertDialog mDialog;

    private static TotalPriceFragment instance;
    public static TotalPriceFragment getInstance(){

        return instance == null ? new TotalPriceFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_total_price, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();

        initView();

        getBundle(getArguments());

        setInformation();

        return layoutView;
    }

    private void initView() {

        mRecyclerShoopingItem.setHasFixedSize(true);
        mRecyclerShoopingItem.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
    }

    private void setInformation() {

        mTxtSalonName.setText(Common.currentSalon.getName());
        mTxtCustomerName.setText(Common.currentBooking.getCustomerName());
        mTxtBarberName.setText(Common.currentBarber.getName());
        mTxtTimeBooking.setText(Common.convertTimeSoltToString(Common.currentBooking.getTimeSlot().intValue()));
        mTxtCustomerPhone.setText(Common.currentBooking.getCustomerPhone());

        if (mHashServicesAdded.size() > 0){

            // Add yo chip
            int tag = 0;
            for (BarberServices barberServices : mHashServicesAdded){

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.raw_chip, null);
                chip.setText(barberServices.getServiceName());
                chip.setTag(tag);

                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mHashServicesAdded.remove(view.getTag());
                        mChipGroup_Services.removeView(view);

                        calculatePrice();
                    }
                });

                mChipGroup_Services.addView(chip);

                tag ++;
            }

        }

        if (mListShopping.size() > 0){

            ShoppingConfirmAdapter mShoppingConfirmAdapter = new ShoppingConfirmAdapter(getActivity(), mListShopping);
            mRecyclerShoopingItem.setAdapter(mShoppingConfirmAdapter);
        }

        calculatePrice();
    }

    private void calculatePrice() {

        double price = Common.DEFAULT_PRICE;

        for (BarberServices barberServices : mHashServicesAdded)
            price += barberServices.getServicePrice();

        for (Shopping shopping : mListShopping)
            price += shopping.getPrice();

        mTxtTotalPrice.setText(new StringBuilder(Common.MONEY_SIGN)
                        .append(price));

    }

    private void getBundle(Bundle arguments) {

        this.mHashServicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());

        this.mListShopping = new Gson()
                .fromJson(arguments.getString(Common.SHOPPING_ITEMS),
                        new TypeToken<List<Shopping>>(){}.getType());
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setCancelable(false)
                .build();

        mIFCMService = RetrofitClient.getInstance()
                .create(IFCMService.class);


    }
}
