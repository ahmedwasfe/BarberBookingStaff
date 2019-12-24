package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.SalonAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.Interface.ISalonLoadListener;
import com.ahmet.barberbookingstaff.Interface.IGetBarberListener;
import com.ahmet.barberbookingstaff.Interface.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.Model.Token;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SalonActivity extends AppCompatActivity
        implements ISalonLoadListener, IUserLoginRemebmberListener, IGetBarberListener {

    @BindView(R.id.search_bar)
    MaterialSearchBar mSearchaSalon;
    @BindView(R.id.recycler_salon)
    RecyclerView mRecyclerSalon;

    private ISalonLoadListener iSalonLoadListener;
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

        mSearchaSalon.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                searechToSalon(text.toString());
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                searechToSalon(text.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void searechToSalon(CharSequence text) {

        FirebaseFirestore.getInstance().collection("AllSalon")
                .whereEqualTo("name", text)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            List<Salon> mListSalon = new ArrayList<>();
                            for (DocumentSnapshot snapshot : task.getResult()){
                                Salon salon = snapshot.toObject(Salon.class);
                                salon.setSalonID(snapshot.getId());
                                mListSalon.add(salon);
                            }
                            iSalonLoadListener.onLoadAllSalonSuccess(mListSalon);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iSalonLoadListener.onLoadAllSalonFailed(e.getMessage());
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
                            iSalonLoadListener.onLoadAllSalonSuccess(mListBranch);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iSalonLoadListener.onLoadAllSalonFailed(e.getMessage());
                    }
                });
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .build();

        iSalonLoadListener = this;
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


      //  String username = Paper.book().read(Common.KEY_LOGGED);
        Log.i("Username_SalonAcivity", user);
    }

    @Override
    public void onGetBarberSuccess(Barber barber) {

        Common.currentBarber = barber;
        Paper.book().write(Common.KEY_BARBER, new Gson().toJson(barber));
    }
}
