package com.ahmet.barberbookingstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Adapter.SalonAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.SpacesItemDecoration;
import com.ahmet.barberbookingstaff.Interface.IBranchLoadListener;
import com.ahmet.barberbookingstaff.Interface.IGetBarberListener;
import com.ahmet.barberbookingstaff.Interface.IOnLoadCountSalon;
import com.ahmet.barberbookingstaff.Interface.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SalonActivity extends AppCompatActivity implements IOnLoadCountSalon, IBranchLoadListener, IUserLoginRemebmberListener, IGetBarberListener {

    @BindView(R.id.txt_salon_count)
    TextView mTxtCountSalon;
    @BindView(R.id.recycler_salon)
    RecyclerView mRecyclerSalon;

    private CollectionReference mSalonReference;

    private IOnLoadCountSalon iOnLoadCountSalon;
    private IBranchLoadListener iBranchLoadListener;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon);

        ButterKnife.bind(this);

        // init recyclerview
        initRecyclerView();

        // init Firebase
        init();

        // load all Salon
        loadSalonBaseOnCity(Common.cityName);

    }

    private void loadSalonBaseOnCity(String cityName) {

        mDialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(cityName)
                .collection("Branch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            iOnLoadCountSalon.onLoadCountSalonSuccess(task.getResult().size());

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

        iOnLoadCountSalon = this;
        iBranchLoadListener = this;
    }

    private void initRecyclerView() {

        mRecyclerSalon.setHasFixedSize(true);
        mRecyclerSalon.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        mRecyclerSalon.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onLoadCountSalonSuccess(int count) {

        mTxtCountSalon.setText(new StringBuilder("All Salon (")
                      .append(count)
                      .append(")"));
    }

    @Override
    public void onLoadAllSalonSuccess(List<Salon> mListBranch) {

        SalonAdapter mSalonAdapter = new SalonAdapter(this, mListBranch, this, this);
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
    }

    @Override
    public void onGetBarberSuccess(Barber barber) {

        Common.currentBarber = barber;
        Paper.book().write(Common.KEY_BARBER, new Gson().toJson(barber));
    }
}
