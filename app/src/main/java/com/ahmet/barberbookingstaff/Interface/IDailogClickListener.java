package com.ahmet.barberbookingstaff.Interface;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public interface IDailogClickListener {

    void onClickPositiveButton(BottomSheetDialog sheetDialog, String username, String password);
    void onClickPositiveButton(BottomSheetDialog sheetDialog,String name, String username, String password, String barberType);
    void onClickNegativeButton(BottomSheetDialog sheetDialog);
}
