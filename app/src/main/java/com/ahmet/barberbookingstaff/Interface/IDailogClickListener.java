package com.ahmet.barberbookingstaff.Interface;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public interface IDailogClickListener {

    void onClickPositiveButton(BottomSheetDialog sheetDialog, String username, String password);
    void onClickNegativeButton(BottomSheetDialog sheetDialog);
}
