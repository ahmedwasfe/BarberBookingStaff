package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
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
import com.ahmet.barberbookingstaff.Model.EventBus.EnableNextButton;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class AddSalonFragment extends Fragment {

    private Unbinder mUnbinder;


    @BindView(R.id.input_salon_name)
    EditText mInputSalonName;
    @BindView(R.id.input_salon_address)
    EditText mInputSalonAddress;
    @BindView(R.id.input_salon_phone)
    EditText mInputSalonPhone;
    @BindView(R.id.input_salon_open_hour)
    EditText mInputSalonOpenHour;
    @BindView(R.id.spinner_salon_type)
    MaterialSpinner mSpinnerSalonType;
    @OnClick(R.id.btn_add_salon)
    void btnAddSalon(){
        verifySalon();
    }

    private String mSalonType = "";

    private AlertDialog mDialog;


    private static AddSalonFragment instance;
    public static AddSalonFragment getInstance(){

        return instance == null ? new AddSalonFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_salon, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        mSpinnerSalonType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                mSalonType = item.toString();
               // Toast.makeText(getContext(), mSalonType, Toast.LENGTH_SHORT).show();
            }
        });


        init();
        selectSalonType();

        return layoutView;
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(getActivity())
                .setMessage("Pleas Wait....")
                .build();;
    }

    private void verifySalon() {

        mDialog.show();

        String salonName = mInputSalonName.getText().toString();
        String salonAddress = mInputSalonAddress.getText().toString();
        String salonPhone = mInputSalonPhone.getText().toString();
        String openHour = mInputSalonOpenHour.getText().toString();

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                if (account != null){

                    Paper.init(getActivity());
                   // Paper.book().write(Common.KEY_LOGGED_EMAIL, account.getEmail());
                    FirebaseFirestore.getInstance().collection("AllSalon")
                            .document(account.getEmail())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()){
                                        DocumentSnapshot snapshot = task.getResult();
                                        if (!snapshot.exists()){
                                            mDialog.dismiss();
                                            addSalon(salonName, account.getEmail(),salonAddress, salonPhone, openHour, mSalonType);

                                        }else {
                                            mDialog.dismiss();
                                            Toast.makeText(getActivity(), "This user exists", Toast.LENGTH_SHORT).show();
                                            Salon salon = new Salon();
                                            salon.setSalonID(account.getEmail());
                                            salon.setEmail(account.getEmail());
                                            Toast.makeText(getActivity(), salon.getSalonID(), Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new EnableNextButton(1, Common.currentSalon));

                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

    }

    private void addSalon(String salonName, String email, String salonAddress,
                          String salonPhone, String openHour, String mSalonType) {

        Salon salon = new Salon(salonName, email, salonAddress, salonPhone, openHour, mSalonType, email);
        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(email)
                .set(salon)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                                mDialog.dismiss();
                                Toast.makeText(getActivity(), "Add Salon Success", Toast.LENGTH_SHORT).show();
                                salon.setSalonID(email);
                                EventBus.getDefault().postSticky(new EnableNextButton(1, Common.currentSalon));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectSalonType(){

        List<String> mListSalonType = new ArrayList<>();
        mListSalonType.add("Please select your salon type");
        mListSalonType.add("Men");
        mListSalonType.add("Women");
        mListSalonType.add("Both");

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mListSalonType);
        mSpinnerSalonType.setAdapter(adapter);
    }

}
