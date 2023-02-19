package com.ahmet.barberbookingstaff.callback;

import com.ahmet.barberbookingstaff.model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {

    void onTimeSoltLoadSuccess(List<BookingInformation> mListTimeSlot);

    void onTimeSoltLoadFailed(String error);

    void onTimeSoltLoadEmpty();
}
