package com.ahmet.barberbookingstaff.SubActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class UpdateProductActivity extends AppCompatActivity {

    @BindView(R.id.scroll_view_update_product)
    ScrollView mScrollView;
    @BindView(R.id.img_update_product)
    ImageView mImgUpdateProduct;
    @BindView(R.id.input_update_product_name)
    TextInputEditText mInputUpdateName;
    @BindView(R.id.input_update_product_price)
    TextInputEditText mInputUpdatePrice;
    @BindView(R.id.input_update_product_description)
    TextInputEditText mInputUpdateDescription;

    private AlertDialog mDialog;

    private Uri mImageUri;

    @OnClick(R.id.btn_delete_product)
    void btnDeleteProduct() {

        deleteProduct();
    }

    @OnClick(R.id.btn_update_product)
    void btnUpdateProduct() {

        String productName = mInputUpdateName.getText().toString();
        Long productPrice = Long.parseLong(mInputUpdatePrice.getText().toString());
        String productDescription = mInputUpdateDescription.getText().toString();

        if (mImageUri == null)
            Common.showSnackBar(this, mScrollView, "");
        else
            updateProduct(productName, productPrice, productDescription);
    }

    private void updateProduct(String productName, Long productPrice, String productDescription) {


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        ButterKnife.bind(this);

        init();

        loadProductInfo();
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(this)
                .setMessage(R.string.please_wait)
                .build();
    }

    private void loadProductInfo() {

        mInputUpdateName.setText(Common.currentProduct.getName());
        mInputUpdatePrice.setText(String.valueOf(Common.currentProduct.getPrice()));
        mInputUpdateDescription.setText(Common.currentProduct.getDescription());
        Picasso.get().load(Common.currentProduct.getImage()).into(mImgUpdateProduct);

        Log.e("PRODUCTID_ACTIVITY", Common.currentProduct.getId());
    }

    private void deleteProduct() {

        mDialog.show();

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .collection(Common.KEY_COLLECTION_PRODUCTS)
                .document(Common.currentProduct.getId())
                .delete()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        mDialog.dismiss();
                        Common.showSnackBar(this, mScrollView, getString(R.string.delete_product_success));
                        startActivity(new Intent(this, HomeStaffActivity.class));
                    }
                });
    }
}
