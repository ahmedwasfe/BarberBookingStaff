package com.ahmet.barberbookingstaff.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.model.CartItem;
import com.ahmet.barberbookingstaff.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ShoppingConfirmAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingHolder> {

    private Context mContext;
    private List<CartItem> mListShopping;
    private LayoutInflater inflater;

    public ShoppingConfirmAdapter(Context mContext, List<CartItem> mListShopping) {
        this.mContext = mContext;
        this.mListShopping = mListShopping;
        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ShoppingAdapter.ShoppingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_shopping, parent, false);
        return new ShoppingAdapter.ShoppingHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingAdapter.ShoppingHolder holder, int position) {


        holder.mTxtShoppingAddToCart.setVisibility(View.GONE);
        holder.mTxtShoppingPrice.setVisibility(View.GONE);

       // ViewGroup.LayoutParams params = holder.mCardShopping.getLayoutParams();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        holder.mCardShopping.setLayoutParams(params);


        holder.mTxtShoppingName.setText(
                new StringBuilder(mListShopping.get(position).getProductName())
                        .append(" x")
                        .append(mListShopping.get(position).getProductQuantity()));
        Picasso.get()
                .load(mListShopping.get(position).getProductImage())
                .placeholder(R.drawable.default_item)
                .into(holder.mImageShoppingItem);


    }

    @Override
    public int getItemCount() {
        return mListShopping.size();
    }
}
