package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {

    void onTimeSoltLoadSuccess(List<BookingInformation> mListTimeSlot);

    void onTimeSoltLoadFailed(String error);

    void onTimeSoltLoadEmpty();
}
