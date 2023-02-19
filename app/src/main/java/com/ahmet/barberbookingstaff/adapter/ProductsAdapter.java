package com.ahmet.barberbookingstaff.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.callback.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.model.Products;
import com.ahmet.barberbookingstaff.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductHolder> {


    private Context mContext;
    private List<Products> mListProducts;
    private LayoutInflater inflater;

    public ProductsAdapter(Context mContext, List<Products> mListProducts) {
        this.mContext = mContext;
        this.mListProducts = mListProducts;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layputView = inflater.inflate(R.layout.raw_products, parent, false);
        return new ProductHolder(layputView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {

        holder.mTxtProductName.setText(Common.formatName(mListProducts.get(position).getName()));
        holder.mTxtProductPrice.setText(new StringBuilder(Common.MONEY_SIGN)
                .append(mListProducts.get(position).getPrice()));
        holder.mTxtProductPrice.setPaintFlags(holder.mTxtProductPrice.getPaintFlags() |
                Paint.STRIKE_THRU_TEXT_FLAG);

        Picasso.get()
                .load(mListProducts.get(position).getImage())
                .placeholder(R.drawable.default_item)
                .into(holder.mImageProduct);

    }

    @Override
    public int getItemCount() {
        return mListProducts.size();
    }

    public Products getItemAtPosition(int position) {
        return mListProducts.get(position);
    }

    static class ProductHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder mUnbinder;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        @BindView(R.id.img_product)
        ImageView mImageProduct;
        @BindView(R.id.txt_name_product)
        TextView mTxtProductName;
        @BindView(R.id.txt_price_product)
        TextView mTxtProductPrice;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}
