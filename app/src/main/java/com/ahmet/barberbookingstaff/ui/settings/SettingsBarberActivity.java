package com.ahmet.barberbookingstaff.ui.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.model.Barber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsBarberActivity extends AppCompatActivity {

    @BindView(R.id.txt_barber_name)
    TextView mTxtBarberName;
    @BindView(R.id.txt_barber_username)
    TextView mTxtBarberUsername;
    @BindView(R.id.txt_barber_type)
    TextView mTxtBarberType;
    @BindView(R.id.txt_barber_status)
    TextView mTxtBarberStatus;
    @BindView(R.id.switch_barber_status)
    Switch mSwitchStatus;
    @BindView(R.id.img_barber_profile)
    CircleImageView mImgProfile;

    private Uri mFileUri;

    private String barbetType;

    private android.app.AlertDialog mDialog;

    private ProgressDialog progressDialog;

    @OnClick(R.id.card_barber_name)
    void showDialogName() {

        showDialogUpdateBarberName();
    }

    @OnClick(R.id.card_barber_type)
    void showDialogType() {

        //  showDialogUpdateBarberType();
    }

    @OnClick(R.id.card_username)
    void showDialogUsername() {
        showDialogChangeUsername();
    }

    @OnClick(R.id.card_password)
    void showDialogPassword() {

        showDialogToChangePassword();
    }

    @OnClick(R.id.img_change_image) void onChangeImageClick(){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"),
                Common.CODE_REQUEST_CHANGE_IMAGE_BARBER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_settings);

        ButterKnife.bind(this);

        init();

        loadBarberInfo();

        mSwitchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                availableBarber(true);

            } else {
                availableBarber(false);

            }
        });
    }

    private void init() {

        progressDialog = new ProgressDialog(this);


    }

    private void showDialogUpdateBarberName() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_update_barber_name, null, false);
        builder.setView(layoutView);

        EditText mInputName = layoutView.findViewById(R.id.input_update_barber_name);

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);

        layoutView.findViewById(R.id.btn_update_barber_name)
                .setOnClickListener(v -> {

                    String barberName = mInputName.getText().toString();
                    if (TextUtils.isEmpty(barberName)) {
                        mInputName.setError(getString(R.string.please_enter_barber_name));
                        return;
                    } else
                        updateBarberName(barberName);
                    dialog.dismiss();
                });

        layoutView.findViewById(R.id.btn_cancel_update_barber_name)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();
    }

    private void showDialogChangeUsername() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_change_username, null, false);
        builder.setView(layoutView);

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);

        EditText mInputUsername = layoutView.findViewById(R.id.input_change_username);
        layoutView.findViewById(R.id.btn_change_username)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String username = mInputUsername.getText().toString();
                        if (TextUtils.isEmpty(username)) {
                            mInputUsername.setError(getString(R.string.please_enter_username));
                            return;
                        } else
                            verfyUsername(username);
                        dialog.dismiss();
                    }
                });

        layoutView.findViewById(R.id.btn_cancel_change_username)
                .setOnClickListener(v -> {
                    dialog.show();
                });

        dialog.show();
    }

    private void showDialogUpdateBarberType() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layourView = LayoutInflater.from(this).inflate(R.layout.dialog_update_barber_type, null, false);
        builder.setView(layourView);

        MaterialSpinner mSpinnerUpdateBarbertype = layourView.findViewById(R.id.spinner_update_barber_type);

        selectBarberType(mSpinnerUpdateBarbertype);

        mSpinnerUpdateBarbertype.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                barbetType = item.toString();
            }
        });

        layourView.findViewById(R.id.btn_update_barber_type)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (TextUtils.isEmpty(barbetType))
                            Toast.makeText(SettingsBarberActivity.this, getString(R.string.please_select_barber_type), Toast.LENGTH_SHORT).show();
                        else
                            updateBarberType(barbetType);
                    }
                });
        builder.create();
        builder.show();
    }

    private void showDialogToChangePassword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null, false);
        builder.setView(layoutView);

        EditText mInputCurrentPassword = layoutView.findViewById(R.id.input_current_password);
        EditText mInputNewPassword = layoutView.findViewById(R.id.input_new_password);
        EditText mInputReTypeNewPassword = layoutView.findViewById(R.id.input_re_type_new_password);

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);


        layoutView.findViewById(R.id.btn_change_password)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String currentPassword = mInputCurrentPassword.getText().toString();
                        String newPassword = mInputNewPassword.getText().toString();
                        String reTypeNewPassword = mInputReTypeNewPassword.getText().toString();

                        if (TextUtils.isEmpty(currentPassword)) {
                            mInputCurrentPassword.setError(getString(R.string.please_enter_current_password));
                            return;
                        }

                        if (currentPassword.length() < 8) {
                            mInputCurrentPassword.setError(getString(R.string.password_must_be_at_least_6));
                            return;
                        }


                        if (TextUtils.isEmpty(newPassword)) {
                            mInputNewPassword.setError(getString(R.string.please_enter_new_password));
                            return;
                        }

                        if (newPassword.length() < 8) {
                            mInputNewPassword.setError(getString(R.string.password_must_be_at_least_6));
                            return;
                        }


                        if (TextUtils.isEmpty(reTypeNewPassword)) {
                            mInputReTypeNewPassword.setError(getString(R.string.please_enter_re_type_password));
                            return;
                        }


                        if (reTypeNewPassword.length() < 6) {
                            mInputReTypeNewPassword.setError(getString(R.string.password_must_be_at_least_6));
                            return;
                        }

                        if (!newPassword.equals(reTypeNewPassword)) {
                            mInputReTypeNewPassword.setError(getString(R.string.the_two_passwords_do_not_match));
                            return;
                        }

                        verifyCurrentBarber(currentPassword, newPassword, dialog);
                    }
                });


        layoutView.findViewById(R.id.btn_cancel_change_password)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();
    }

    private void verfyUsername(String username) {

        progressDialog.show();

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .orderByChild("username").equalTo(Common.currentBarber.getUsername())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.exists()) {
                            changeUsername(username);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsBarberActivity.this, getString(R.string.this_user_not_exists), Toast.LENGTH_SHORT).show();

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void changeUsername(String username) {

        Map<String, Object> mMapUsername = new HashMap<>();
        mMapUsername.put("username", username);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId())
                .updateChildren(mMapUsername)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsBarberActivity.this, getString(R.string.change_username_success), Toast.LENGTH_SHORT).show();
                            loadBarberInfo();
                        }
                    }
                });
    }

    private void updateBarberType(String barbetType) {

        progressDialog.show();

        Map<String, Object> mMapBarberType = new HashMap<>();
        mMapBarberType.put("barberType", barbetType);

        FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
                .document(Common.currentSalon.getSalonId())
                .collection(Common.KEY_BARBER_REFERANCE)
                .document(Common.currentBarber.getBarberId())
                .update(mMapBarberType)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsBarberActivity.this, getString(R.string.update_barber_type_success), Toast.LENGTH_SHORT).show();
                            loadBarberInfo();
                        }
                    }
                });
    }

    private void updateBarberName(String barberName) {

        progressDialog.show();

        Map<String, Object> mMapUpdateName = new HashMap<>();
        mMapUpdateName.put("name", barberName);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId())
                .updateChildren(mMapUpdateName)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsBarberActivity.this, getString(R.string.update_barber_name_success), Toast.LENGTH_SHORT).show();
                        loadBarberInfo();
                    }
                });
    }

    private void verifyCurrentBarber(String currentPassword, String newPassword, AlertDialog dialog) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .orderByChild("username").equalTo(mTxtBarberUsername.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.exists()) {
                            changePassword(currentPassword, newPassword, dialog);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SettingsBarberActivity.this, getString(R.string.this_user_not_exists), Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void changePassword(String currentPassword, String newPassword, AlertDialog dialog) {

        progressDialog.show();

        Map<String, Object> mMapNewPassword = new HashMap<>();
        mMapNewPassword.put("password", newPassword);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .orderByChild("password").equalTo(currentPassword)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.exists()) {

                            FirebaseDatabase.getInstance().getReference()
                                    .child(Common.KEY_AllSALON_REFERANCE)
                                    .child(Common.currentSalon.getSalonId())
                                    .child(Common.KEY_BARBER_REFERANCE)
                                    .child(Common.currentBarber.getBarberId())
                                    .updateChildren(mMapNewPassword)
                                    .addOnCompleteListener(task1 -> {

                                        if (task1.isSuccessful()) {
                                            progressDialog.dismiss();
                                            dialog.dismiss();
                                            Toast.makeText(SettingsBarberActivity.this, getString(R.string.change_password_success), Toast.LENGTH_SHORT).show();
                                            loadBarberInfo();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            dialog.show();
                            Toast.makeText(SettingsBarberActivity.this, getString(R.string.current_password_incorect), Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void availableBarber(boolean isAvailable) {

        Map<String, Object> mMapBarberStaus = new HashMap<>();
        mMapBarberStaus.put("available", isAvailable);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId())
                .updateChildren(mMapBarberStaus)
                .addOnCompleteListener(task -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_AllSALON_REFERANCE)
                            .child(Common.currentSalon.getSalonId())
                            .child(Common.KEY_BARBER_REFERANCE)
                            .child(Common.currentBarber.getBarberId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        Barber barber = dataSnapshot.getValue(Barber.class);
                                        if (barber.isAvailable() == 1)
                                            mTxtBarberStatus.setText(getString(R.string.barber_avaliable));
                                        else
                                            mTxtBarberStatus.setText(getString(R.string.barber_busy));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                });
    }

    private void loadBarberInfo() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            Barber barber = dataSnapshot.getValue(Barber.class);
                            Common.currentBarber = barber;

                            mTxtBarberName.setText(barber.getName());
                            mTxtBarberUsername.setText(barber.getUsername());
                            mTxtBarberType.setText(barber.getBarberType());
                            if (barber.isAvailable() == 1)
                                mSwitchStatus.setChecked(true);

                            if (barber.isAvailable() == 1)
                                mTxtBarberStatus.setText(getString(R.string.barber_avaliable));
                            else
                                mTxtBarberStatus.setText(getString(R.string.barber_busy));

                            Picasso.get().load(barber.getImage()).placeholder(R.drawable.hairdresser).into(mImgProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void selectBarberType(MaterialSpinner materialSpinner) {

        List<String> mListBarbeType = new ArrayList<>();
        mListBarbeType.add(getString(R.string.please_select_barber_type));
        mListBarbeType.add(getString(R.string.admin));
        mListBarbeType.add(getString(R.string.staff));

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mListBarbeType);
        materialSpinner.setAdapter(arrayAdapter);
    }

    private void uploadImage(){

        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.show();

        String uniqueImageName = UUID.randomUUID().toString();

        StorageReference storageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(Common.KEY_IMAGES_PROFILE_PATH + uniqueImageName);
        storageFolder.putFile(mFileUri)
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    storageFolder.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                updateBarberImage(uri.toString());
                            });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            progressDialog.setMessage(new StringBuilder(getString(R.string.uploading_done)+" ").append(progress).append("%"));
        });
    }

    private void updateBarberImage(String imageUri) {

        progressDialog.show();

        Map<String, Object> mapImage = new HashMap<>();
        mapImage.put("image", imageUri);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_BARBER_REFERANCE)
                .child(Common.currentBarber.getBarberId())
                .updateChildren(mapImage)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        loadBarberInfo();
                        progressDialog.dismiss();
                        Toast.makeText(this, getString(R.string.update_image_success), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.CODE_REQUEST_CHANGE_IMAGE_BARBER){
            if (resultCode == RESULT_OK){
                mFileUri = data.getData();
                mImgProfile.setImageURI(mFileUri);
                uploadImage();
            }
        }
    }
}
