package com.ahmet.barberbookingstaff.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Interface.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.Model.Products;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
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

        holder.mTxtProductName.setText(Common.formatShoppingName(mListProducts.get(position).getName()));
        holder.mTxtProductPrice.setText(new StringBuilder("$ ").append(mListProducts.get(position).getPrice()));
        Picasso.get()
                .load(mListProducts.get(position).getImage())
                .placeholder(R.drawable.barbersalon)
                .into(holder.mImageProduct);

        holder.mRemoveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance().collection("AllSalon")
                        .document(Common.currentSalon.getSalonID())
                        .collection("Products")
                        .document(mListProducts.get(position).getId())
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(mContext, "Removed products Successfull", Toast.LENGTH_SHORT).show();
                                    notifyDataSetChanged();
                                }
                            }
                        });
            }
        });

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {
                Toast.makeText(mContext, mListProducts.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return mListProducts.size();
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
        @BindView(R.id.txt_remove)
        TextView mRemoveProduct;

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
