package com.ahmet.barberbookingstaff.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Services;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ServicesAdaper extends RecyclerView.Adapter<ServicesAdaper.ServicesHolder> {

    private Context mContext;
    private List<Services> mListServices;
    private LayoutInflater inflater;

    public ServicesAdaper(Context mContext, List<Services> mListServices) {
        this.mContext = mContext;
        this.mListServices = mListServices;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ServicesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_service, parent, false);
        return new ServicesHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicesHolder holder, int position) {

        holder.mTxtServiceName.setText(mListServices.get(position).getServiceName());
        holder.mTxtServicePrice.setText(new StringBuilder(Common.MONEY_SIGN)
                                .append(mListServices.get(position).getServicePrice()));

        holder.mTxtServicePrice.setPaintFlags(holder.mTxtServicePrice.getPaintFlags() |
                Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public int getItemCount() {
        return mListServices.size();
    }

    public Services getItemAtPosition(int position) {
        return mListServices.get(position);
    }

    class ServicesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_service_name)
        TextView mTxtServiceName;
        @BindView(R.id.txt_service_price)
        TextView mTxtServicePrice;

        public ServicesHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}