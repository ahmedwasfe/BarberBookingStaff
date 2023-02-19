package com.ahmet.barberbookingstaff.common;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.ahmet.barberbookingstaff.model.Notifications.GetNotification;

import java.util.List;

public class DiffCallBack extends DiffUtil.Callback {

    private List<GetNotification> mOldGetNotificationList;
    private List<GetNotification> mNewGetNotificationList;

    public DiffCallBack(List<GetNotification> mOldGetNotificationList, List<GetNotification> mNewGetNotificationList) {
        this.mOldGetNotificationList = mOldGetNotificationList;
        this.mNewGetNotificationList = mNewGetNotificationList;
    }

    @Override
    public int getOldListSize() {
        return mOldGetNotificationList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewGetNotificationList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

        return
                mOldGetNotificationList.get(oldItemPosition).getUuid()
                        == mNewGetNotificationList.get(newItemPosition).getUuid();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldGetNotificationList.get(oldItemPosition) == mNewGetNotificationList.get(newItemPosition);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
