package com.ahmet.barberbookingstaff.ui.staff;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class AddStaffFragment extends Fragment {


    private AlertDialog mDialog;

    @BindView(R.id.input_barber_name)
    EditText mInputBarberName;
    @BindView(R.id.input_barber_username)
    EditText mInputbarberUsername;
    @BindView(R.id.input_barber_password)
    EditText mInputBarberPassword;
    @BindView(R.id.spinner_barber_type)
    MaterialSpinner mSpinnerBarberType;
    @BindView(R.id.scroll_view_add_staff)
    ScrollView mScrollView;


    @OnClick(R.id.btn_add_barber)
    void btnAddBarber() {

        String barberName = mInputBarberName.getText().toString();
        String username = mInputbarberUsername.getText().toString();
        String password = mInputBarberPassword.getText().toString();

        if (TextUtils.isEmpty(barberName)) {
            mInputBarberName.setError(getString(R.string.please_enter_barber_name));
            return;
        }

        if (TextUtils.isEmpty(username)) {
            mInputbarberUsername.setError(getString(R.string.please_enter_username));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mInputBarberPassword.setError(getString(R.string.please_enter_password));
            return;
        }

        if (password.length() < 8) {
            mInputBarberPassword.setError(getString(R.string.password_must_be_at_least_6));
            return;
        }


        if (TextUtils.isEmpty(mBarberType))
            Common.showSnackBar(getActivity(), mScrollView, getString(R.string.please_select_barber_type));
        else
            verifyBarber(barberName, username, password);
    }


    private String mBarberType = "";

    private static AddStaffFragment instance;

    public static AddStaffFragment getInstance() {
        return instance == null ? new AddStaffFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_staff, container, false);

        ButterKnife.bind(this, layoutView);


        mSpinnerBarberType.setOnItemSelectedListener((view, position, id, item) -> mBarberType = item.toString());

//        mSpinnerBarberType.setItems(barber.getBarberType());

        init();
        selectBarberType();

        return layoutView;
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(getActivity())
                .setMessage(R.string.please_wait)
                .build();
        ;
    }

    private void verifyBarber(String barberName, String username, String password) {

        mDialog.show();

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            mDialog.dismiss();
                            addStaff(barberName, username, password, mBarberType);
                        }else {
                            mDialog.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.this_user_exists), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void addStaff(String barberName, String username, String password, String barberType) {



        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            String barberId = UUID.randomUUID().toString();

                            if (dataSnapshot.child(Common.KEY_FIELD_BARBER_TYPE).getValue().equals(Common.KEY_VALUE_BARBER_TYPE)) {

                                Barber barber = new Barber(barberId, barberName, username, password,Common.KEY_IMAGE_DEFAULT, barberType, 0);

                                FirebaseDatabase.getInstance().getReference()
                                        .child(Common.KEY_AllSALON_REFERANCE)
                                        .child(Common.currentSalon.getSalonId())
                                        .child(Common.KEY_BARBER_REFERANCE)
                                        .child(barberId)
                                        .setValue(barber)
                                        .addOnCompleteListener(task11 -> {
                                            if (task11.isSuccessful()) {
                                                mDialog.dismiss();
                                                Toast.makeText(getActivity(), getString(R.string.add_barber_success), Toast.LENGTH_SHORT).show();
                                                Common.setFragment(StaffFragment.getInstance(), R.id.frame_layout_home,
                                                        getActivity().getSupportFragmentManager());
                                            }
                                        }).addOnFailureListener(e -> {
                                    mDialog.dismiss();
                                    Log.e("TAG_ADD_BARBER", e.getMessage());
                                });

                            } else
                                Toast.makeText(getActivity(), getString(R.string.can_not_add_barber), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void selectBarberType() {

        List<String> mListSalonType = new ArrayList<>();
        mListSalonType.add(getString(R.string.select_barber_type));
        mListSalonType.add(getString(R.string.admin));
        mListSalonType.add(getString(R.string.staff));

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mListSalonType);
        mSpinnerBarberType.setAdapter(adapter);
    }
}