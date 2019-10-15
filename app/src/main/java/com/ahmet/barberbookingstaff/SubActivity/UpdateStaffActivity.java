package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class UpdateStaffActivity extends AppCompatActivity {

    private Unbinder mUnbinder;

    @BindView(R.id.txt_input_staff_name_update)
    TextInputEditText mInputName;
    @BindView(R.id.txt_input_staff_username_update)
    TextInputEditText mInputUsername;
    @BindView(R.id.txt_input_staff_password_update)
    TextInputEditText mInputPassword;
    @BindView(R.id.spinner_barber_type_update)
    MaterialSpinner mSpinnerStaffType;

    private String mStaffType = "";

    private AlertDialog mDialog;

    @OnClick(R.id.btn_update_staff)
    void updateStaff(){
        verifyStaff();
    }

    @BindView(R.id.btn_remove_staff)
    Button mBtnRemove;
    @OnClick(R.id.btn_remove_staff)
    void clickRemoveStaff(){
        removeStaff();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_staff);

        mUnbinder = ButterKnife.bind(this);

        getSupportActionBar().setTitle("Staff Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                            if (!snapshot.get("barberType").equals("Admin")){
                                // Toast.makeText(UpdateStaffActivity.this, snapshot.get("barberType").toString(), Toast.LENGTH_SHORT).show();
                                mBtnRemove.setVisibility(View.GONE);
                            }else {
                                if (snapshot.get("username").equals(Common.currentBarber.getUsername())){
                                   // Toast.makeText(UpdateStaffActivity.this, snapshot.getString("username") + " from database", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(UpdateStaffActivity.this, Common.currentBarber.getUsername() + " from App", Toast.LENGTH_SHORT).show();
                                    mInputUsername.setEnabled(true);
                                    mInputPassword.setEnabled(true);
                                }else {
                                    mInputUsername.setEnabled(false);
                                    mInputPassword.setEnabled(false);
                                }

                            }
                        }
                    }
                });

//        if (getIntent() != null){
//
//            Barber barber = getIntent().getParcelableExtra("barber");
//
//            mInputName.setText(barber.getName());
//            mInputUsername.setText(barber.getUsername());
//            mInputPassword.setText(barber.getPassword());
//        }

        mInputName.setText(Common.currentBarber.getName());
        mInputUsername.setText(Common.currentBarber.getUsername());
        mInputPassword.setText(Common.currentBarber.getPassword());

        mSpinnerStaffType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                mStaffType = item.toString();
            }
        });


//        mSpinnerBarberType.setItems(barber.getBarberType());

        init();
        selectStaffType();

      // mSpinnerStaffType.setItems(Common.currentBarber.getBarberType());

       // Toast.makeText(this, Common.currentBarber.getName(), Toast.LENGTH_SHORT).show();
       // Toast.makeText(this, Common.currentBarber.getBarberID(), Toast.LENGTH_SHORT).show();
    }


    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(this)
                .setMessage("Please wait...")
                .build();;
    }

    private void verifyStaff() {

        mDialog.show();

        String staffName = mInputName.getText().toString();
        String username = mInputUsername.getText().toString();
        String password = mInputPassword.getText().toString();

        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()){
//                                    DocumentSnapshot snapshot = task.getResult();
//                                    if (!snapshot.exists()){
                            mDialog.dismiss();
                            updateStaff(Common.currentSalon.getEmail(), staffName, username, password, mStaffType);

//                                    }else {
//                                        mDialog.dismiss();
//                                        Toast.makeText(getActivity(), "This user exists", Toast.LENGTH_SHORT).show();
//                                    }
                        }
                    }
                });


    }

    private void updateStaff(String email, String name, String username, String password, String barberType) {


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


                                Map<String, Object> mMapUpdateStaff = new HashMap<>();
                                mMapUpdateStaff.put("name", name);
                                mMapUpdateStaff.put("username", username);
                                mMapUpdateStaff.put("password", password);
                                mMapUpdateStaff.put("barberType", barberType);

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


                                                        FirebaseFirestore.getInstance().collection("AllSalon")
                                                                .document(email)
                                                                .collection("Barber")
                                                                .document(Common.currentBarber.getBarberID())
                                                                .update(mMapUpdateStaff)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            mDialog.dismiss();
                                                                            Toast.makeText(UpdateStaffActivity.this, "Updated Staff success", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mDialog.dismiss();
                                                                Log.e("TAG_ADD_BARBER", e.getMessage());
                                                            }
                                                        });

                                                    }else {

                                                        mDialog.dismiss();
                                                        Toast.makeText(UpdateStaffActivity.this, "This User not exists Please try again", Toast.LENGTH_SHORT).show();
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

                            }else

                                Toast.makeText(UpdateStaffActivity.this, "Can not add Barber", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void removeStaff(){

        mDialog.show();

        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().size() > 0){

                                FirebaseFirestore.getInstance().collection("AllSalon")
                                        .document(Common.currentSalon.getSalonID())
                                        .collection("Barber")
                                        .document(Common.currentBarber.getBarberID())
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mDialog.dismiss();
                                                    Toast.makeText(UpdateStaffActivity.this, "Removed Staff Success", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            }else {
                                mDialog.dismiss();
                                Toast.makeText(UpdateStaffActivity.this, "This User not exists Please try again", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });


    }

    private void selectStaffType(){

        List<String> mListStaffType = new ArrayList<>();
        mListStaffType.add("Please select your Barber Type");
        mListStaffType.add("Admin");
        mListStaffType.add("Staff");

        ArrayAdapter adapter = new ArrayAdapter(UpdateStaffActivity.this, android.R.layout.simple_spinner_item, mListStaffType);
        mSpinnerStaffType.setAdapter(adapter);
    }
}
