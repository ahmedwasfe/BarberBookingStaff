package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.SalonAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.Interface.IBranchLoadListener;
import com.ahmet.barberbookingstaff.Interface.IGetBarberListener;
import com.ahmet.barberbookingstaff.Interface.IOnLoadCountSalon;
import com.ahmet.barberbookingstaff.Interface.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.MainActivity;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SalonActivity extends AppCompatActivity
        implements IBranchLoadListener, IUserLoginRemebmberListener, IGetBarberListener {

    @BindView(R.id.searchable_spinner)
    SearchableSpinner mSearchableSpinner;
    @BindView(R.id.recycler_salon)
    RecyclerView mRecyclerSalon;

    private IBranchLoadListener iBranchLoadListener;
    private List<Salon> mListSalon;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon);

        ButterKnife.bind(this);

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (task.isSuccessful()){

                            Common.updateToken(SalonActivity.this,
                                    task.getResult().getToken());
                            Log.d("TOKEN", task.getResult().getToken());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SalonActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        Paper.init(this);
        String user = Paper.book().read(Common.KEY_LOGGED);
        // If user not login before
        if (TextUtils.isEmpty(user)){

            // init recyclerview
            initRecyclerView();

            // init Firebase
            init();

            // load all Salon
            loadAllSalon();

        } else{

            Gson gson = new Gson();
            Common.currentSalon = gson.fromJson(Paper.book().read(Common.KEY_SALON,""),
                    new TypeToken<Salon>(){}.getType());
            Common.currentBarber = gson.fromJson(Paper.book().read(Common.KEY_BARBER,""),
                    new TypeToken<Barber>(){}.getType());

            Intent intent = new Intent(SalonActivity.this, HomeStaffActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        mSearchableSpinner.setTitle("Please Select Salon");
        mSearchableSpinner.setKeepScreenOn(true);
        mSearchableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void loadAllSalon() {

        mDialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            List<Salon> mListBranch = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Salon salon = documentSnapshot.toObject(Salon.class);
                                salon.setSalonID(documentSnapshot.getId());
                                mListBranch.add(salon);
                            }
                            iBranchLoadListener.onLoadAllSalonSuccess(mListBranch);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iBranchLoadListener.onLoadAllSalonFailed(e.getMessage());
                    }
                });
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .build();

        iBranchLoadListener = this;
    }

    private void initRecyclerView() {

        mRecyclerSalon.setHasFixedSize(true);
        mRecyclerSalon.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        mRecyclerSalon.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onLoadAllSalonSuccess(List<Salon> mListSalons) {

        SalonAdapter mSalonAdapter = new SalonAdapter(this, mListSalons, this, this);
        mRecyclerSalon.setAdapter(mSalonAdapter);
        mDialog.dismiss();;

        List<String> mListSalonName = new ArrayList<>();
        for (Salon salon : mListSalons){
            mListSalonName.add(salon.getName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListSalonName);
            mSearchableSpinner.setAdapter(adapter);
        }
    }

    @Override
    public void onLoadAllSalonFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void onUserLoginSuccess(String user) {
        // Save user
        Paper.init(this);
        Paper.book().write(Common.KEY_LOGGED, user);
        Paper.book().write(Common.KEY_CITY, Common.cityName);
        Paper.book().write(Common.KEY_SALON, new Gson().toJson(Common.currentSalon));
    }

    @Override
    public void onGetBarberSuccess(Barber barber) {

        Common.currentBarber = barber;
        Paper.book().write(Common.KEY_BARBER, new Gson().toJson(barber));
    }
}
