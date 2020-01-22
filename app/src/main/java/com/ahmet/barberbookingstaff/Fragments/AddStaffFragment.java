package com.ahmet.barberbookingstaff.Fragments;

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

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class AddStaffFragment extends Fragment {

    private Unbinder mUnbinder;


    private AlertDialog mDialog;

    @BindView(R.id.txt_input_barber_name)
    EditText mInputBarberName;
    @BindView(R.id.txt_input_barber_username)
    EditText mInputbarberUsername;
    @BindView(R.id.txt_input_barber_password)
    EditText mInputBarberPassword;
    @BindView(R.id.spinner_barber_type)
    MaterialSpinner mSpinnerBarberType;
    @BindView(R.id.scroll_view_add_staff)
    ScrollView mScrollView;


    @OnClick(R.id.btn_add_barber)
    void btnAddBarber(){

        String barberName = mInputBarberName.getText().toString();
        String username = mInputbarberUsername.getText().toString();
        String password = mInputBarberPassword.getText().toString();

        if (TextUtils.isEmpty(barberName)){
            mInputBarberName.setError(getString(R.string.please_enter_barber_name));
            return;
        }

        if (TextUtils.isEmpty(username)){
            mInputbarberUsername.setError(getString(R.string.please_enter_username));
            return;
        }

        if (TextUtils.isEmpty(password)){
            mInputBarberPassword.setError(getString(R.string.please_enter_password));
            return;
        }

        if (password.length() < 6){
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
    public static AddStaffFragment getInstance(){

        return instance == null ? new AddStaffFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_staff, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);


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
                .build();;
    }

    private void verifyBarber(String barberName, String username, String password) {

        mDialog.show();

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
//                                    DocumentSnapshot snapshot = task.getResult();
//                                    if (!snapshot.exists()){
                        mDialog.dismiss();
                        addStaff(Common.currentSalon.getEmail(), barberName, username, password, mBarberType);

//                                    }else {
//                                        mDialog.dismiss();
//                                        Toast.makeText(getActivity(), "This user exists", Toast.LENGTH_SHORT).show();
//                                    }
                    }
                });


    }

    private void addStaff(String email, String barberName, String username, String password, String barberType) {


        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .collection(Common.KEY_COLLECTION_BARBER)
                .document(Common.currentBarber.getBarberID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot.get("barberType").equals("Admin")){

                            Barber barber = new Barber(barberName, username, password, barberType);

                            FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                                    .document(email)
                                    .collection(Common.KEY_COLLECTION_BARBER)
                                    .whereEqualTo("username", username)
                                    .get()
                                    .addOnCompleteListener(task1 -> {

                                        if (task1.isSuccessful()){
                                            if (task1.getResult().size() > 0){

                                                mDialog.dismiss();
                                                Toast.makeText(getActivity(), getString(R.string.username_exists), Toast.LENGTH_SHORT).show();

                                            }else {


                                                FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                                                        .document(email)
                                                        .collection(Common.KEY_COLLECTION_BARBER)
                                                        .add(barber)
                                                        .addOnCompleteListener(task11 -> {
                                                            if (task11.isSuccessful()){
                                                                mDialog.dismiss();
                                                                Toast.makeText(getActivity(), getString(R.string.add_barber_success), Toast.LENGTH_SHORT).show();

                                                                task11.getResult().get()
                                                                        .addOnCompleteListener(task111 -> Log.e("Barber_ID", task111.getResult().getId()));
                                                            }
                                                        }).addOnFailureListener(e -> {
                                                            mDialog.dismiss();
                                                            Log.e("TAG_ADD_BARBER", e.getMessage());
                                                        });

                                            }
                                        }

                                    }).addOnFailureListener(e -> {
                                        mDialog.dismiss();
                                        Log.e("TAG_GET_BARBER_", e.getMessage());
                                    });

                        }else
                            Toast.makeText(getActivity(), getString(R.string.can_not_add_barber), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void selectBarberType(){

        List<String> mListSalonType = new ArrayList<>();
        mListSalonType.add(getString(R.string.select_barber_type));
        mListSalonType.add(getString(R.string.admin));
        mListSalonType.add(getString(R.string.staff));

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mListSalonType);
        mSpinnerBarberType.setAdapter(adapter);
    }
}