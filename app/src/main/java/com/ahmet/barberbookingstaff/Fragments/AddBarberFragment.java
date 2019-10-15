package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.ahmet.barberbookingstaff.Interface.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.SubActivity.SalonActivity;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
    void btnAddBarber(){
        verifyBarber();
    }


    private String mBarberType = "";

    private AlertDialog mDialog;


    private static AddBarberFragment instance;
    public static AddBarberFragment getInstance(){

        return instance == null ? new AddBarberFragment() : instance;
    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_barber, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        userListener = this;

        mSpinnerBarberType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                mBarberType = item.toString();
            }
        });


        init();
        selectBarberType();


//        Toast.makeText(getActivity(), Common.currentSalon.getSalonID(), Toast.LENGTH_SHORT).show();


        return layoutView;
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(getActivity())
                .setMessage("Please wait...")
                .build();;
    }

    private void verifyBarber() {

        mDialog.show();

        String barberName = mInputBarberName.getText().toString();
        String username = mInputbarberUsername.getText().toString();
        String password = mInputBarberPassword.getText().toString();


        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {

                FirebaseFirestore.getInstance().collection("AllSalon")
                        .document(account.getEmail())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (task.isSuccessful()){
//                                    DocumentSnapshot snapshot = task.getResult();
//                                    if (!snapshot.exists()){
                                        mDialog.dismiss();
                                        addBarber(account.getEmail(), barberName, username, password, mBarberType);

//                                    }else {
//                                        mDialog.dismiss();
//                                        Toast.makeText(getActivity(), "This user exists", Toast.LENGTH_SHORT).show();
//                                    }
                                }
                            }
                        });

            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

    }

    private void addBarber(String email, String barberName, String username, String password, String barberType) {


        Barber barber = new Barber(barberName, username, password, barberType);

        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(email)
                .collection("Barber")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){
                            if (task.getResult().size() > 0){

                                mDialog.dismiss();
                               // Toast.makeText(getContext(), "This barber Exists", Toast.LENGTH_SHORT).show();

                                userListener.onUserLoginSuccess(username);

                                Barber barber = new Barber();
                                for (DocumentSnapshot snapshot : task.getResult()){
                                    barber = snapshot.toObject(Barber.class);
                                    barber.setBarberID(snapshot.getId());
                                    Log.e("Barber_ID", barber.getBarberID());
                                }


                                Intent intent = new Intent(getActivity(), SalonActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                getActivity().finish();

                            }else {


                                FirebaseFirestore.getInstance().collection("AllSalon")
                                        .document(email)
                                        .collection("Barber")
                                        .add(barber)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()){
                                                    mDialog.dismiss();
                                                    Toast.makeText(getActivity(), "Added barber success", Toast.LENGTH_SHORT).show();

                                                    task.getResult().get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    Log.e("Barber_ID", task.getResult().getId());
                                                                }
                                                            });
                                                    Intent intent = new Intent(getActivity(), SalonActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    getActivity().finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        Log.e("TAG_ADD_BARBER", e.getMessage());
                                    }
                                });

                                userListener.onUserLoginSuccess(username);
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
              Log.e("TAG_GET_BARBER_", e.getMessage());
            }
        });
    }

    private void selectBarberType(){

        List<String> mListSalonType = new ArrayList<>();
        mListSalonType.add("Please select your Barber Type");
        mListSalonType.add("Admin");
        mListSalonType.add("Staff");

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
