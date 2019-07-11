package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.CityAdapter;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.Interface.IAllCitiesLoadListener;
import com.ahmet.barberbookingstaff.Model.City;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements IAllCitiesLoadListener {

    @BindView(R.id.recycler_state)
    RecyclerView mRecyclerCity;

    private CollectionReference mCityReference;

    private IAllCitiesLoadListener iAllCitiesLoadListener;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // init recyclerview
        initRecyclerView();

        // init Firebase
        init();

        loadAllCitiesFromDatabase();
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
