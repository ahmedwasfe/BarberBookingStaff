package com.ahmet.barberbookingstaff.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.Interface.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;

import java.util.ArrayList;
import java.util.List;

public class SalonAdapter extends RecyclerView.Adapter<SalonAdapter.SalonHolder> {

    private Context mContext;
    private List<Salon> mListSalon;
    private List<CardView> mListCard;
    private LayoutInflater inflater;

    LocalBroadcastManager mLocalBroadcastManager;

    public SalonAdapter(Context mContext, List<Salon> mListSalon) {
        this.mContext = mContext;
        this.mListSalon = mListSalon;
        inflater = LayoutInflater.from(mContext);
        mListCard = new ArrayList<>();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
    }

    @NonNull
    @Override
    public SalonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_salon, parent, false);
        return new SalonHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull SalonHolder holder, int position) {

        holder.mTxtSalonName.setText(mListSalon.get(position).getName());
        holder.mTxtSalonAddress.setText(mListSalon.get(position).getAddress());

        holder.setItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mListSalon.size();
    }



    static class SalonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTxtSalonName, mTxtSalonAddress;
        CardView mCardSalon;

        IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public SalonHolder(@NonNull View itemView) {
            super(itemView);

            mTxtSalonName = itemView.findViewById(R.id.txt_salon_name);
            mTxtSalonAddress = itemView.findViewById(R.id.txt_salon_address);
            mCardSalon = itemView.findViewById(R.id.card_salon);

            itemView.setOnClickListener(this);
        }

        public void setItemSelectedListener(IRecyclerItemSelectedListener mIRecyclerItemSelectedListener) {
            this.mIRecyclerItemSelectedListener = mIRecyclerItemSelectedListener;
        }



        @Override
        public void onClick(View view) {
            mIRecyclerItemSelectedListener.onItemSelected(view, getAdapterPosition());
        }

    }
}
