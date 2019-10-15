package com.ahmet.barberbookingstaff.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Interface.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.Model.City;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.SubActivity.SalonActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityHolder> {

    private Context mContext;
    private List<City> mListCity;
    private LayoutInflater inflater;

    private int lastPosition = -1;

    public CityAdapter(Context mContext, List<City> mListCity) {
        this.mContext = mContext;
        this.mListCity = mListCity;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public CityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_city, parent, false);
        return new CityHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull CityHolder holder, int position) {

        holder.mTxtCityName.setText(mListCity.get(position).getName());

        setAnimation(holder.itemView, position);

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {

                Common.cityName = mListCity.get(position).getName();
                mContext.startActivity(new Intent(mContext, SalonActivity.class));
            }
        });
    }

    private void setAnimation(View itemView, int position) {

        if (position > lastPosition){

            Animation animation = AnimationUtils.loadAnimation(mContext,
                    android.R.anim.slide_in_left);
            itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mListCity.size();
    }

    static class CityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_city_name)
        TextView mTxtCityName;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public CityHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelected(view, getAdapterPosition());
        }
    }
}
