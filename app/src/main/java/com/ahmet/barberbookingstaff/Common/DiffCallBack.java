package com.ahmet.barberbookingstaff.Common;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.ahmet.barberbookingstaff.Model.Notification;

import java.util.List;

public class DiffCallBack extends DiffUtil.Callback {

    private List<Notification> mOldNotificationList;
    private List<Notification> mNewNotificationList;

    public DiffCallBack(List<Notification> mOldNotificationList, List<Notification> mNewNotificationList) {
        this.mOldNotificationList = mOldNotificationList;
        this.mNewNotificationList = mNewNotificationList;
    }

    @Override
    public int getOldListSize() {
        return mOldNotificationList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewNotificationList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

        return
                mOldNotificationList.get(oldItemPosition).getUuid()
                        == mNewNotificationList.get(newItemPosition).getUuid();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldNotificationList.get(oldItemPosition) == mNewNotificationList.get(newItemPosition);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
