package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.TextView;

import com.ahmet.barberbookingstaff.Adapter.ViewPagerAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.NonSwipeViewPager;
import com.ahmet.barberbookingstaff.Model.EventBus.BarberEvent;
import com.ahmet.barberbookingstaff.Model.EventBus.EnableNextButton;
import com.ahmet.barberbookingstaff.Model.EventBus.SalonEvent;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shuhart.stepview.StepView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddSalonActivity extends AppCompatActivity {

    private Unbinder mUnbinder;

    @BindView(R.id.step_view)
    StepView mStepView;
    @BindView(R.id.non_view_pager)
    NonSwipeViewPager mViewPager;
    @BindView(R.id.txt_next)
    TextView mTxtNext;
    @BindView(R.id.txt_previous)
    TextView mTxtPrevious;

    @OnClick(R.id.txt_next)
    void nextStep(){
        if (Common.step < 2 || Common.step == 0){

            Common.step++;
            if (Common.step == 1){

                EventBus.getDefault().postSticky(new SalonEvent(Common.currentSalon));

            } else if (Common.step == 2){
                if (Common.currentSalon != null)
                    EventBus.getDefault().postSticky(new BarberEvent(Common.currentBarber));
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

        getSupportActionBar().setTitle("Add Salon");

        setupViewPager();
        setupStepView();
        setColorStep();

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {

                FirebaseFirestore.getInstance().collection("AllSalon")
                        .document(account.getEmail())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    if (task.getResult().exists()){
                                        mTxtNext.setEnabled(true);
                                        setColorStep();
                                    }
                                }
                            }
                        });
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

    }

    private void setupStepView() {

        List<String> mListStep = new ArrayList<>();
        mListStep.add("Salon");
        mListStep.add("Barber");
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
