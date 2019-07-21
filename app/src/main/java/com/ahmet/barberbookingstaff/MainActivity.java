package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.CityAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.Interface.IAllCitiesLoadListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.City;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterActivity;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements IAllCitiesLoadListener {

    @BindView(R.id.recycler_state)
    RecyclerView mRecyclerCity;

    private CollectionReference mCityReference;

    private IAllCitiesLoadListener iAllCitiesLoadListener;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dexter.withActivity(this)
                .withPermissions(new String [] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                }).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {


                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {

                                if (task.isSuccessful()){

                                    Common.updateToken(MainActivity.this,
                                            task.getResult().getToken());
                                    Log.d("TOKEN", task.getResult().getToken());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                Paper.init(MainActivity.this);
                String user = Paper.book().read(Common.KEY_LOGGED);
                // If user not login before
                if (TextUtils.isEmpty(user)) {

                    setContentView(R.layout.activity_main);

                    ButterKnife.bind(MainActivity.this);

                    // init recyclerview
                    initRecyclerView();

                    // init Firebase
                    init();

                    loadAllCitiesFromDatabase();

                } else {  // If user already login

                    Gson gson = new Gson();
                    Common.cityName = Paper.book().read(Common.KEY_CITY);
                    Common.currentSalon = gson.fromJson(Paper.book().read(Common.KEY_SALON, ""),
                            new TypeToken<Salon>(){}.getType());
                    Common.currentBarber = gson.fromJson(Paper.book().read(Common.KEY_BARBER, ""),
                            new TypeToken<Barber>(){}.getType());

                    Intent intent = new Intent(MainActivity.this, HomeStaffActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();

    }

    private void loadAllCitiesFromDatabase() {

        mDialog.show();

        mCityReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){

                            List<City> mListCity = new ArrayList<>();

                            for (DocumentSnapshot documentSnapshot : task.getResult()){
                                City city = documentSnapshot.toObject(City.class);
                                mListCity.add(city);
                            }

                            iAllCitiesLoadListener.onAllCitiesLoadSuccess(mListCity);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAllCitiesLoadListener.onAllCitiesLoadFailed(e.getMessage());
            }
        });
    }

    private void init() {

        mCityReference = FirebaseFirestore.getInstance().collection("AllSalon");

        iAllCitiesLoadListener = this;

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .build();
    }

    private void initRecyclerView() {

        mRecyclerCity.setHasFixedSize(true);
        mRecyclerCity.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        mRecyclerCity.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onAllCitiesLoadSuccess(List<City> mListCity) {

        CityAdapter mCityAdapter = new CityAdapter(MainActivity.this, mListCity);
        mRecyclerCity.setAdapter(mCityAdapter);
        mDialog.dismiss();
    }

    @Override
    public void onAllCitiesLoadFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }
}
