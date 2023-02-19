package com.ahmet.barberbookingstaff.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.callback.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.callback.IShoppingItemSelectedListener;
import com.ahmet.barberbookingstaff.model.Products;
import com.ahmet.barberbookingstaff.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingHolder> {

    private Context mContext;
    private List<Products> mListProducts;
    private LayoutInflater inflater;
   // private CartDatabase cartDatabase;
    private IShoppingItemSelectedListener mIShoppingItemSelectedListener;

    public ShoppingAdapter(Context mContext, List<Products> mListProducts, IShoppingItemSelectedListener mIShoppingItemSelectedListener) {
        this.mContext = mContext;
        this.mListProducts = mListProducts;
        inflater = LayoutInflater.from(mContext);
       // cartDatabase = CartDatabase.getInstance(mContext);
        this.mIShoppingItemSelectedListener = mIShoppingItemSelectedListener;
    }

    @NonNull
    @Override
    public ShoppingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(mContext)
                .inflate(R.layout.raw_shopping, parent, false);

        return new ShoppingHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingHolder holder, int position) {


        holder.mTxtShoppingName.setText(Common.formatName(mListProducts.get(position).getName()));
        holder.mTxtShoppingPrice.setText(new StringBuilder("$ ").append(mListProducts.get(position).getPrice()));

        Picasso.get()
                .load(mListProducts.get(position).getImage())
                .placeholder(R.drawable.default_item)
                .into(holder.mImageShoppingItem);

        // Add to cart
        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {

                mIShoppingItemSelectedListener.onShoppingItemSelected(mListProducts.get(position));
                Toast.makeText(mContext, mContext.getString(R.string.add_to_shopping), Toast.LENGTH_SHORT).show();
            }


        });

    }

    @Override
    public int getItemCount() {
        return mListProducts.size();
    }

    static class ShoppingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImageShoppingItem;
        TextView mTxtShoppingName, mTxtShoppingPrice, mTxtShoppingAddToCart;
        CardView mCardShopping;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public ShoppingHolder(@NonNull View itemView) {
            super(itemView);

            mImageShoppingItem = itemView.findViewById(R.id.img_shopping_item);
            mTxtShoppingName = itemView.findViewById(R.id.txt_name_shopping_item);
            mTxtShoppingPrice = itemView.findViewById(R.id.txt_price_shopping_item);
            mTxtShoppingAddToCart = itemView.findViewById(R.id.txt_shopping_add_cart);
            mCardShopping = itemView.findViewById(R.id.card_shopping);

            mTxtShoppingAddToCart.setOnClickListener(this);
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
