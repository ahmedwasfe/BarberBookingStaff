package com.ahmet.barberbookingstaff.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ahmet.barberbookingstaff.callback.IDailogClickListener;
import com.ahmet.barberbookingstaff.callback.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.Token;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class SheetDialogLogin extends BottomSheetBehavior implements IUserLoginRemebmberListener {


    @BindView(R.id.input_new_barber_name)
    EditText mInputBarberName;

    @BindView(R.id.txt_input_login_username)
    EditText mInputLoginUsername;
    @BindView(R.id.input_new_username)
    EditText mInputNewUsername;

    @BindView(R.id.txt_input_login_password)
    EditText mInputLoginPassword;
    @BindView(R.id.input_new_password)
    EditText mInputNewPassword;

    @BindView(R.id.text_title)
    TextView mTextTitle;

    @BindView(R.id.txt_login_barber)
    TextView mTxtLogin;
    @BindView(R.id.txt_create_new_barber)
    TextView mTxtNewBarber;
    @BindView(R.id.txt_forget_password)
    TextView mTxtForgetPassword;

    @BindView(R.id.btn_login)
    Button mBtnLogin;
    @BindView(R.id.btn_new_barber)
    Button mBtnNewBarber;

    @BindView(R.id.spinner_new_barber_type)
    MaterialSpinner mSpinnerBarberType;

    @BindView(R.id.constraint_login)
    ConstraintLayout mConstraintLogin;
    @BindView(R.id.constraint_new_barber)
    ConstraintLayout mConstraintNewBarber;

    private String mBarberType;

    private ProgressDialog mDialog;

    private IUserLoginRemebmberListener iUserLoginRemebmberListener;

    public static SheetDialogLogin mDailogLogin;

    public IDailogClickListener iDailogClickListener;

    public static SheetDialogLogin getInstance() {
        if (mDailogLogin == null) {
            mDailogLogin = new SheetDialogLogin();
        }

        return mDailogLogin;
    }

    public void showLoginDailog(Context mContext, String email, String title, String textLogin, String negativeText,
                                IDailogClickListener iDailogClickListener) {

        this.iDailogClickListener = iDailogClickListener;
        iUserLoginRemebmberListener = this;

        mDialog = new ProgressDialog(mContext);

        BottomSheetDialog mSheetDialog = new BottomSheetDialog(mContext);
        mSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSheetDialog.setContentView(R.layout.sheet_dialog_login);

//        View layoutView = LayoutInflater.from(mContext)
//                .inflate(R.layout.sheet_dialog_login, null);

        ButterKnife.bind(this, mSheetDialog);

        mSheetDialog.show();

        mTextTitle.setText(mContext.getString(R.string.log_in));

        mSpinnerBarberType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                mBarberType = item.toString();
            }
        });

        selectBarberType(mContext, mSpinnerBarberType);

        Window window = mSheetDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mBtnLogin.setOnClickListener(view -> {

            String username = mInputLoginUsername.getText().toString();
            String password = mInputLoginPassword.getText().toString();

            if (TextUtils.isEmpty(username)) {
                mInputLoginUsername.setError(mContext.getString(R.string.please_enter_username));
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mInputLoginPassword.setError(mContext.getString(R.string.please_enter_password));
                return;
            }

            if (password.length() < 6) {
                mInputLoginPassword.setError(mContext.getString(R.string.password_must_be_at_least_6));
                return;
            }

            iDailogClickListener.onClickPositiveButton(mSheetDialog, username, password);
        });

        mBtnNewBarber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barberName = mInputBarberName.getText().toString();
                String username = mInputNewUsername.getText().toString();
                String password = mInputNewPassword.getText().toString();

                if (TextUtils.isEmpty(barberName)) {
                    mInputBarberName.setError(mContext.getString(R.string.please_enter_barber_name));
                    return;
                }

                if (TextUtils.isEmpty(username)) {
                    mInputNewUsername.setError(mContext.getString(R.string.please_enter_username));
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mInputNewPassword.setError(mContext.getString(R.string.please_enter_password));
                    return;
                }

                if (password.length() < 8) {
                    mInputNewPassword.setError(mContext.getString(R.string.password_must_be_at_least_6));
                    return;
                }

                if (TextUtils.isEmpty(mBarberType))
                    Common.showSnackBar(mContext, mSpinnerBarberType, mContext.getString(R.string.please_select_salon_type));
                else
                    verifyBarber(mContext, email, barberName, username, password);
            }
        });

        mTxtNewBarber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mConstraintLogin.setVisibility(View.GONE);
                mConstraintNewBarber.setVisibility(View.VISIBLE);
                mTextTitle.setText(mContext.getString(R.string.new_barber));

            }
        });

        mTxtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mConstraintLogin.setVisibility(View.VISIBLE);
                mConstraintNewBarber.setVisibility(View.GONE);
                mTextTitle.setText(mContext.getString(R.string.log_in));

            }
        });

    }

    private void verifyBarber(Context mContext, String email, String barberName, String username, String password) {

        mDialog.show();

        FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
                .document(email)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
//
                        mDialog.dismiss();
                        addBarber(mContext, email, barberName, username, password, mBarberType);
                    }
                });


    }

    private void addBarber(Context mContext, String email, String barberName, String username, String password, String barberType) {


        Barber barber = new Barber(barberName, username, password, barberType);

        FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
                .document(email)
                .collection(Common.KEY_BARBER_REFERANCE)
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {

                            mDialog.dismiss();
                             Toast.makeText(mContext, "This barber Exists", Toast.LENGTH_SHORT).show();

                            // userListener.onUserLoginSuccess(username);

//                            Barber barber1 = new Barber();
//                            for (DocumentSnapshot snapshot : task.getResult()) {
//                                barber1 = snapshot.toObject(Barber.class);
//                                // barber1.setBarberID(snapshot.getId());
//                                Log.e("Barber_ID", barber1.getBarberID());
//
//                            }

                        } else {


                            FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
                                    .document(email)
                                    .collection(Common.KEY_BARBER_REFERANCE)
                                    .add(barber)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {

                                            FirebaseInstanceId.getInstance()
                                                    .getInstanceId()
                                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                            if (task.isSuccessful()){

                                                                Token token = new Token(task.getResult().getToken(), username, Common.TOKEN_TYPE.BARBER);
                                                                FirebaseFirestore.getInstance()
                                                                        .collection(Common.KEY_TOKENS_REFERANCE)
                                                                        .document(username)
                                                                        .set(token)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });


                                            mDialog.dismiss();
                                            Toast.makeText(mContext, mContext.getString(R.string.add_barber_success), Toast.LENGTH_SHORT).show();
                                            mConstraintLogin.setVisibility(View.VISIBLE);
                                            mConstraintNewBarber.setVisibility(View.GONE);
                                            mTextTitle.setText(mContext.getString(R.string.log_in));
                                        }
                                    }).addOnFailureListener(e -> {
                                mDialog.dismiss();
                                Log.e("TAG_ADD_BARBER", e.getMessage());
                            });

                            //  userListener.onUserLoginSuccess(username);
                        }
                    }

                }).addOnFailureListener(e -> {
            mDialog.dismiss();
            Log.e("TAG_GET_BARBER_", e.getMessage());
        });
    }

    private void selectBarberType(Context mContext, MaterialSpinner materialSpinner) {

        List<String> mListSalonType = new ArrayList<>();
        mListSalonType.add(mContext.getString(R.string.select_barber_type));
        mListSalonType.add(mContext.getString(R.string.admin));
        mListSalonType.add(mContext.getString(R.string.staff));

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, mListSalonType);
        materialSpinner.setAdapter(adapter);
    }

    @Override
    public void onUserLoginSuccess(Context mContext, String user) {

        Paper.init(mContext);
        Paper.book().write(Common.KEY_LOGGED, user);
    }
}
