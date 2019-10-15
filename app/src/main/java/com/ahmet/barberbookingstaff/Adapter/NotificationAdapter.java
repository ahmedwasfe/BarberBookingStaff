package com.ahmet.barberbookingstaff.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.Common.DiffCallBack;
import com.ahmet.barberbookingstaff.Model.Notification;
import com.ahmet.barberbookingstaff.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {

    private Context mContext;
    private List<Notification> mListNotification;
    private LayoutInflater inflater;

    public NotificationAdapter(Context mContext, List<Notification> mListNotification) {
        this.mContext = mContext;
        this.mListNotification = mListNotification;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_notification, parent, false);
        return new NotificationHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {

        holder.mTxtTitle.setText(mListNotification.get(position).getTitle());
        holder.mTxtContent.setText(mListNotification.get(position).getContent());
        Picasso.get().load(R.drawable.hairdresser).into(holder.mImgUser);

    }

    @Override
    public int getItemCount() {
        return mListNotification.size();
    }

    public void updateList(List<Notification> mNewListNotifications) {

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(this.mListNotification, mNewListNotifications));
        mListNotification.addAll(mNewListNotifications);
        diffResult.dispatchUpdatesTo(this);
    }

    static class NotificationHolder extends RecyclerView.ViewHolder{

        ImageView mImgUser;
        TextView mTxtTitle, mTxtContent;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);

            mImgUser = itemView.findViewById(R.id.image_notification_user);
            mTxtTitle = itemView.findViewById(R.id.txt_notification_title);
            mTxtContent = itemView.findViewById(R.id.txt_notification_content);
        }
    }
}
