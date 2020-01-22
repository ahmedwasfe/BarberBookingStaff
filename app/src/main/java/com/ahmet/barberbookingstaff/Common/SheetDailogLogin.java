package com.ahmet.barberbookingstaff.Common;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ahmet.barberbookingstaff.Interface.IDailogClickListener;
import com.ahmet.barberbookingstaff.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SheetDailogLogin {

    @BindView(R.id.txt_title)
    TextView mTxtTitle;
    @BindView(R.id.txt_input_username)
    EditText mInputUsername;
    @BindView(R.id.txt_input_password)
    EditText mInputPassword;
    @BindView(R.id.btn_login)
    Button mBtnLogin;
    @BindView(R.id.btn_login_add_barber)
    Button mBtnAddBarber;

    public static SheetDailogLogin mDailogLogin;

    public IDailogClickListener iDailogClickListener;

    public static SheetDailogLogin getInstance(){
        if (mDailogLogin == null){
            mDailogLogin = new SheetDailogLogin();
        }

        return mDailogLogin;
    }

    public void showLoginDailog(Context mContext, String title, String positiveText, String negativeText,
                                IDailogClickListener iDailogClickListener){

        this.iDailogClickListener = iDailogClickListener;

        BottomSheetDialog mSheetDialog = new BottomSheetDialog(mContext);
        mSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSheetDialog.setContentView(R.layout.sheet_dialog_login);

//        View layoutView = LayoutInflater.from(mContext)
//                .inflate(R.layout.sheet_dialog_login, null);

        ButterKnife.bind(this, mSheetDialog);

        if (!TextUtils.isEmpty(title)){
            mTxtTitle.setText(title);
            mTxtTitle.setVisibility(View.VISIBLE);
        }

        mBtnLogin.setText(positiveText);
        mBtnAddBarber.setText(negativeText);

      //  mSheetDialog.setCancelable(false);
       // mSheetDialog.setCanceledOnTouchOutside(false);
        mSheetDialog.show();

        Window window = mSheetDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mBtnLogin.setOnClickListener(view -> {

            String username = mInputUsername.getText().toString();
            String password = mInputPassword.getText().toString();

            if (TextUtils.isEmpty(username)){
                mInputUsername.setError(mContext.getString(R.string.please_enter_username));
                return;
            }

            if (TextUtils.isEmpty(password)){
                mInputPassword.setError(mContext.getString(R.string.please_enter_password));
                return;
            }

            if (password.length() < 6){
                mInputPassword.setError(mContext.getString(R.string.password_must_be_at_least_6));
                return;
            }

            iDailogClickListener.onClickPositiveButton(mSheetDialog, username, password);
        });

        mBtnAddBarber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iDailogClickListener.onClickNegativeButton(mSheetDialog);
            }
        });

    }
}
