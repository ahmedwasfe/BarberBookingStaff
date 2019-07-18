package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.TimeSlot;

import java.util.List;

public interface ITimeSlotLoadListener {

    void onTimeSoltLoadSuccess(List<TimeSlot> mListTimeSlot);

    void onTimeSoltLoadFailed(String error);

    void onTimeSoltLoadEmpty();
}
