package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.ViewPagerAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.NonSwipeViewPager;
import com.ahmet.barberbookingstaff.Model.EventBus.BarberEvent;
import com.ahmet.barberbookingstaff.Model.EventBus.EnableNextButton;
import com.ahmet.barberbookingstaff.Model.EventBus.SalonEvent;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shuhart.stepview.StepView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddSalonActivity extends AppCompatActivity {

    private Unbinder mUnbinder;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private PlacesClient mPlaceClient;
    private Location mLastLocation;

    @BindView(R.id.step_view)
    StepView mStepView;
    @BindView(R.id.non_view_pager)
    NonSwipeViewPager mViewPager;
    @BindView(R.id.txt_next)
    TextView mTxtNext;
    @BindView(R.id.txt_previous)
    TextView mTxtPrevious;

    String currentSalonEmail = "";

    @OnClick(R.id.txt_next)
    void nextStep(){
        if (Common.step < 3 || Common.step == 0){

            Common.step++;
            if (Common.step == 1){

                EventBus.getDefault().postSticky(new SalonEvent(Common.currentSalon));

            }else if (Common.step == 2){

                if (Common.currentSalon != null)
                    EventBus.getDefault().postSticky(new BarberEvent(Common.currentBarber));

            } else if (Common.step == 3){
//                if (Common.currentSalon != null)
//                    EventBus.getDefault().postSticky(new BarberEvent(Common.currentBarber));
            }

            mViewPager.setCurrentItem(Common.step);
        }
    }

    @OnClick(R.id.txt_previous)
    void previousStep(){

        if (Common.step == 2 || Common.step > 0){

            Common.step--;
            mViewPager.setCurrentItem(Common.step);

            if (Common.step < 2) {
                mTxtNext.setEnabled(true);
                setColorStep();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_salon);

        mUnbinder = ButterKnife.bind(
                this);

        getSupportActionBar().setTitle(R.string.new_salon);

        setupViewPager();
        setupStepView();
        setColorStep();



    }



    private void setupStepView() {

        List<String> mListStep = new ArrayList<>();
        mListStep.add(getString(R.string.email));
        mListStep.add(getString(R.string.salon_));
        mListStep.add(getString(R.string.barber_));
        mStepView.setSteps(mListStep);
    }

    private void setupViewPager(){

        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(3);  // i have four fragment so i need keep state of this four page
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                // Show step
                mStepView.go(position, true);

                if (position == 0){
                    mTxtPrevious.setEnabled(false);
                }else{
                    mTxtPrevious.setEnabled(true);
                }

                // Set disable button next here
                mTxtNext.setEnabled(false);
                setColorStep();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setColorStep() {

        if (mTxtNext.isEnabled()){
            mTxtNext.setTextColor(getResources().getColor(R.color.colorAccent));
        }else {
            mTxtNext.setTextColor(getResources().getColor(R.color.colorGray));
        }

        if (mTxtPrevious.isEnabled()){
            mTxtPrevious.setTextColor(getResources().getColor(R.color.colorAccent));
        }else {
            mTxtPrevious.setTextColor(getResources().getColor(R.color.colorGray));
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void enableNext(EnableNextButton event){

        int step = event.getStep();

        if (step == 1)
            Common.currentSalon = event.getSalon();
        else if (step == 2)
            Common.currentSalon = event.getSalon();
        else if (step == 3)
            Common.currentBarber = event.getBarber();

        mTxtNext.setEnabled(true);
        setColorStep();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
