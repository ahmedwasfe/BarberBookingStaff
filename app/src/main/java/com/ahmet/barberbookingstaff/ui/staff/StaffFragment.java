package com.ahmet.barberbookingstaff.ui.staff;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.adapter.StaffAdapter;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Barber;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class StaffFragment extends Fragment {


    @BindView(R.id.recycler_show_staff)
    RecyclerView mRecyclerStaff;
    @BindView(R.id.shimmer_layout_staff)
    ShimmerLayout mShimmerLayout;

    private StaffViewModel staffViewModel;
    private StaffAdapter staffAdapter;


    private static StaffFragment instance;

    public static StaffFragment getInstance() {
        return instance == null ? new StaffFragment() : instance;
    }

    @OnClick(R.id.fab_add_barber)
    void onAddBarberClick() {
        Common.setFragment(AddStaffFragment.getInstance(), R.id.frame_layout_home,
                getActivity().getSupportFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        staffViewModel = new ViewModelProvider(this).get(StaffViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_show_staff, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        staffViewModel.getMutableMessageError()
                .observe(getViewLifecycleOwner(), error -> {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                });

        staffViewModel.getListMutableBarber()
                .observe(getViewLifecycleOwner(), barbers -> {

                    mShimmerLayout.stopShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);

                    staffAdapter = new StaffAdapter(getActivity(), barbers);
                    mRecyclerStaff.setAdapter(staffAdapter);
                });

        return layoutView;
    }

//    private void loadAllStaff() {
//
//        mDialog.show();
//
//        FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
//                .document(Common.currentSalon.getSalonID())
//                .collection(Common.KEY_BARBER_REFERANCE)
//                .document(Common.currentBarber.getBarberID())
//                .get()
//                .addOnCompleteListener(task -> {
//
//                    if (task.isSuccessful()){
//
//                        DocumentSnapshot snapshot = task.getResult();
//                        if (snapshot.get(Common.KEY_FIELD_BARBER_TYPE).equals(Common.KEY_VALUE_BARBER_TYPE)){
//
//                            FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
//                                    .document(Common.currentSalon.getSalonID())
//                                    .collection(Common.KEY_BARBER_REFERANCE)
//                                    .get()
//                                    .addOnCompleteListener(task1 -> {
//
//                                        if (task1.isSuccessful()){
//
//                                            List<Barber> mListStaff = new ArrayList<>();
//                                            for (DocumentSnapshot snapshot1 : task1.getResult()){
//                                                Barber barber = snapshot1.toObject(Barber.class);
//                                                mListStaff.add(barber);
//                                            }
//
//                                            iStaffLoadCallbackListener.onLoadStaffSuccess(mListStaff);
//                                        }
//                                    }).addOnFailureListener(e -> {
//                                        mDialog.dismiss();
//                                        iStaffLoadCallbackListener.onLoadStaffFailed(e.getMessage());
//                                    });
//
//                        }else {
//                            mDialog.dismiss();
//                           // Toast.makeText(getActivity(), "You can not show all Staff", Toast.LENGTH_SHORT).show();
//
//
//                            FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
//                                    .document(Common.currentSalon.getSalonID())
//                                    .collection(Common.KEY_BARBER_REFERANCE)
//                                    .document(Common.currentBarber.getBarberID())
//                                    .get()
//                                    .addOnCompleteListener(task12 -> {
//
//                                        if (task12.isSuccessful()){
//
//                                            List<Barber> mListStaff = new ArrayList<>();
//                                            DocumentSnapshot documentSnapshot = task12.getResult();
//
//                                             mListStaff.add(new Barber(
//                                                     documentSnapshot.get(Common.KEY_FIELD_BARBER_NAME).toString(),
//                                                     documentSnapshot.get(Common.KEY_FIELD_BARBER_USERNAME).toString(),
//                                                     documentSnapshot.get(Common.KEY_FIELD_BARBER_PASSWORD).toString(),
//                                                     documentSnapshot.get(Common.KEY_FIELD_BARBER_TYPE).toString()));
//
//                                            iStaffLoadCallbackListener.onLoadStaffSuccess(mListStaff);
//                                        }
//
//                                    });
//                        }
//                    }
//                });
//    }

    private void initViews() {

        mShimmerLayout.startShimmerAnimation();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerStaff.setHasFixedSize(true);
        mRecyclerStaff.setLayoutManager(layoutManager);
        mRecyclerStaff.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

//        SwipeRecyclerViewHelper swipeRecyclerView = new SwipeRecyclerViewHelper(
//                getActivity(), mRecyclerStaff, width / 6) {
//            @Override
//            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {
//
//                mListMButton.add(new MButton(getActivity(), getString(R.string.delete), 24, 0,
//                        getActivity().getColor(R.color.colorRed), position -> {
//
//                    Barber barber = staffAdapter.getItemAtPosition(position);
//                    showDialogDeleteBarber(barber, position);
//                }));
//
//                mListMButton.add(new MButton(getActivity(), getString(R.string.update), 24, 0,
//                        getActivity().getColor(R.color.colorGreen), position -> {
//
//                    Barber barber = staffAdapter.getItemAtPosition(position);
//
//                }));
//            }
//        };


    }

    private void showDialogDeleteBarber(Barber barber, int position) {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.remove_staff)
                .setMessage(R.string.are_sure_remove_staff)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).setPositiveButton(R.string.ok, (dialog, which) -> {
            deleteBarber(barber, position);
        }).show();
    }

    private void deleteBarber(Barber barber, int position) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {


                            if (barber.getBarberType().equals(Common.currentBarber.getBarberType())) {
                                Toast.makeText(getActivity(), "can not remove admin", Toast.LENGTH_SHORT).show();

                            } else{
                                if (Common.currentBarber.getBarberType().equals("Admin")) {

                                    FirebaseDatabase.getInstance().getReference()
                                            .child(Common.KEY_AllSALON_REFERANCE)
                                            .child(Common.currentSalon.getSalonId())
                                            .child(Common.KEY_BARBER_REFERANCE)
                                            .child(barber.getBarberId())
                                            .removeValue()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    staffViewModel.loadAllStaff();
                                                    Toast.makeText(getActivity(), getString(R.string.remove_staff_success), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }else
                                    Toast.makeText(getActivity(), getString(R.string.can_not_remove_barber), Toast.LENGTH_SHORT).show();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}