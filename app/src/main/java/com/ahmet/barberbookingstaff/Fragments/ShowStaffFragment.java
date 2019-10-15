package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ahmet.barberbookingstaff.Adapter.StaffAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Interface.IStaffLoadListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ShowStaffFragment extends Fragment  implements IStaffLoadListener {

    private Unbinder mUnbinder;

    @BindView(R.id.recycler_show_staff)
    RecyclerView mRecyclerStaff;


    private AlertDialog mDialog;

    private IStaffLoadListener iStaffLoadListener;

    private static ShowStaffFragment instance;
    public static ShowStaffFragment getInstance(){

        if (instance == null)
            instance = new ShowStaffFragment();

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_show_staff, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();

        loadAllStaff();


        return layoutView;
    }

    private void loadAllStaff() {

        mDialog.show();

        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()){

                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.get("barberType").equals("Admin")){

                                FirebaseFirestore.getInstance().collection("AllSalon")
                                        .document(Common.currentSalon.getSalonID())
                                        .collection("Barber")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                if (task.isSuccessful()){

                                                    List<Barber> mListStaff = new ArrayList<>();
                                                    for (DocumentSnapshot snapshot : task.getResult()){
                                                        Barber barber = snapshot.toObject(Barber.class);
                                                        mListStaff.add(barber);
                                                    }

                                                    iStaffLoadListener.onLoadStaffSuccess(mListStaff);
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        iStaffLoadListener.onLoadStaffFailed(e.getMessage());
                                    }
                                });

                            }else {
                                mDialog.dismiss();
                               // Toast.makeText(getActivity(), "You can not show all Staff", Toast.LENGTH_SHORT).show();


                                FirebaseFirestore.getInstance().collection("AllSalon")
                                        .document(Common.currentSalon.getSalonID())
                                        .collection("Barber")
                                        .document(Common.currentBarber.getBarberID())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                if (task.isSuccessful()){

                                                    List<Barber> mListStaff = new ArrayList<>();
                                                    DocumentSnapshot snapshot = task.getResult();

                                                     mListStaff.add(new Barber(snapshot.get("name").toString(),
                                                                snapshot.get("username").toString(),
                                                                snapshot.get("password").toString(),
                                                                snapshot.get("barberType").toString()));

                                                    iStaffLoadListener.onLoadStaffSuccess(mListStaff);
                                                }

                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void init() {

        mRecyclerStaff.setHasFixedSize(true);
        mRecyclerStaff.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));

        iStaffLoadListener = this;

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(getActivity())
                .setMessage("Please wait...")
                .build();;
    }

    @Override
    public void onLoadStaffSuccess(List<Barber> mListStaff) {

        StaffAdapter adapter = new StaffAdapter(getActivity(), mListStaff);
        mRecyclerStaff.setAdapter(adapter);

        mDialog.dismiss();
    }

    @Override
    public void onLoadStaffFailed(String error) {

        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();

        mDialog.dismiss();
    }
}
