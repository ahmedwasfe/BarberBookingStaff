package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.HomeActivity;
import com.ahmet.barberbookingstaff.Interface.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.SubActivity.SalonActivity;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class AddBarberFragment extends Fragment implements IUserLoginRemebmberListener {

    private Unbinder mUnbinder;


    @BindView(R.id.txt_input_barber_name)
    EditText mInputBarberName;
    @BindView(R.id.txt_input_barber_username)
    EditText mInputbarberUsername;
    @BindView(R.id.txt_input_barber_password)
    EditText mInputBarberPassword;
    @BindView(R.id.spinner_barber_type)
    MaterialSpinner mSpinnerBarberType;

    private IUserLoginRemebmberListener userListener;

    @OnClick(R.id.btn_add_barber)
    void btnAddBarber() {

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
            Common.showSnackBar(getActivity(), mSpinnerBarberType, getString(R.string.please_select_salon_type));
        else
            verifyBarber(barberName, username, password);
    }


    private String mBarberType = "";

    private AlertDialog mDialog;


    private static AddBarberFragment instance;

    public static AddBarberFragment getInstance() {

        return instance == null ? new AddBarberFragment() : instance;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_barber, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        userListener = this;

        mSpinnerBarberType.setOnItemSelectedListener((view, position, id, item) -> mBarberType = item.toString());


        init();
        selectBarberType();


//        Toast.makeText(getActivity(), Common.currentSalon.getSalonID(), Toast.LENGTH_SHORT).show();


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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                    .document(user.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
//                                    DocumentSnapshot snapshot = task.getResult();
//                                    if (!snapshot.exists()){
                            mDialog.dismiss();
                            addBarber(user.getEmail(), barberName, username, password, mBarberType);

//                                    }else {
//                                        mDialog.dismiss();
//                                        Toast.makeText(getActivity(), "This user exists", Toast.LENGTH_SHORT).show();
//                                    }
                        }
                    });
        }


    }

    private void addBarber(String email, String barberName, String username, String password, String barberType) {


        Barber barber = new Barber(barberName, username, password, barberType);

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(email)
                .collection(Common.KEY_COLLECTION_BARBER)
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {

                            mDialog.dismiss();
                            // Toast.makeText(getContext(), "This barber Exists", Toast.LENGTH_SHORT).show();

                            userListener.onUserLoginSuccess(username);

                            Barber barber1 = new Barber();
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                barber1 = snapshot.toObject(Barber.class);
                               // barber1.setBarberID(snapshot.getId());
                                Log.e("Barber_ID", barber1.getBarberID());

                            }
//
//                            Intent intent = new Intent(getActivity(), SalonActivity.class);
////                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
////                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            Common.currentSalon.setSalonID(email);
//                            startActivity(intent);
//                            getActivity().finish();

                        } else {


                            FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                                    .document(email)
                                    .collection(Common.KEY_COLLECTION_BARBER)
                                    .add(barber)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            mDialog.dismiss();
                                            Toast.makeText(getActivity(), getString(R.string.add_barber_success), Toast.LENGTH_SHORT).show();

                                            task1.getResult().get()
                                                    .addOnCompleteListener(task11 ->
                                                            Log.e("Barber_ID", task11.getResult().getId()));
                                            Log.e("SALONID", email);

                                            Intent intent = new Intent(getActivity(), SalonActivity.class);
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                          //  Common.currentSalon.setSalonID(email);
                                            startActivity(intent);
                                           // getActivity().finish();
                                        }
                                    }).addOnFailureListener(e -> {
                                mDialog.dismiss();
                                Log.e("TAG_ADD_BARBER", e.getMessage());
                            });

                            userListener.onUserLoginSuccess(username);
                        }
                    }

                }).addOnFailureListener(e -> {
            mDialog.dismiss();
            Log.e("TAG_GET_BARBER_", e.getMessage());
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

    @Override
    public void onUserLoginSuccess(String username) {

        Paper.init(getActivity());
        Paper.book().write(Common.KEY_LOGGED, username);
//        Paper.book().write(Common.KEY_SALON, Common.currentSalon);
    }
}