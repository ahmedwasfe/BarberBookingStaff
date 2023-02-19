package com.ahmet.barberbookingstaff.ui.product;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Products;
import com.ahmet.barberbookingstaff.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class AddProductFragment extends Fragment {


    @BindView(R.id.scroll_view_add_product)
    ScrollView mScrollView;
    @BindView(R.id.img_add_product)
    ImageView mImgAddproduct;
    @BindView(R.id.input_product_name)
    EditText mInputProductName;
    @BindView(R.id.input_product_price)
    EditText mInputProductPrice;
    @BindView(R.id.input_product_description)
    EditText mInputProductDescription;


    private Uri mFileUri;
   // private String imageUri = "";
    private Bitmap selectedImageBitmap;
//    private byte[] imageByte;

    private double mProgress = 0;

    private StorageReference mStorageRef;

    private AlertDialog mDialog;


    @OnClick(R.id.btn_add_product)
    void submitProduct() {

        String name = mInputProductName.getText().toString();
        String price = mInputProductPrice.getText().toString();
        String descriptopion = mInputProductDescription.getText().toString();

        if (TextUtils.isEmpty(name)) {
            mInputProductName.setError(getString(R.string.please_enter_product_name));
            return;
        }

        Products products = new Products();
        products.setName(name);
        products.setPrice(TextUtils.isEmpty(price) ? 0 : Long.parseLong(price));
        products.setDescription(descriptopion);

        if (mFileUri != null)
            uploadImage(products);
        else
            addProductsToSalon(products);


    }

    @OnClick(R.id.img_add_product)
    void selectImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mFileUri = Common.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        intent.setType("image/*");
        startActivityForResult(intent, Common.CODE_REQUEST_GALLERY);
    }


    private static AddProductFragment instance;

    public static AddProductFragment getInstance() {
        return instance == null ? new AddProductFragment() : instance;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_product, container, false);

        ButterKnife.bind(this, layoutView);

        init();

        return layoutView;
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setCancelable(false)
                .setMessage(R.string.loadding_product)
                .build();


    }



    private void uploadImage(Products products) {

        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();

        String filaName = UUID.randomUUID().toString();
        String filePath = new StringBuilder(Common.KEY_IMAGES_PRODUCT_PATH)
                .append(filaName)
                .toString();

        StorageReference storageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(filePath);
        storageFolder.putFile(mFileUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            mDialog.dismiss();
            storageFolder.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        addProductsToSalon(products);
                    });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done) + " ").append(progress).append("%"));
        });

    }

    private void oldCodeUploadImage(){
        mStorageRef = FirebaseStorage.getInstance().getReference().child("filePath");
        UploadTask uploadTask = mStorageRef.putFile(mFileUri);
        uploadTask.addOnProgressListener(taskSnapshot -> {

            double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            if (currentProgress > (mProgress + 15)) {
                mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

            }
        }).continueWithTask(task -> {

            if (!task.isSuccessful())
                Toast.makeText(getActivity(), getString(R.string.can_not_upload_image), Toast.LENGTH_SHORT).show();
            return mStorageRef.getDownloadUrl();

        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

               // imageUri = task.getResult().toString();
              //  Log.i("DOWNLIADABLELINK", imageUri);


              //  addProductsToSalon("name", imageUri, 0, "descriptopion");
            }
        });
    }

    private void addProductsToSalon(Products products) {

        String productId = UUID.randomUUID().toString();

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_PRODUCTS_REFERANCE)
                .child(productId)
                .setValue(products)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        restData();
                        Toast.makeText(getActivity(), getString(R.string.upload_product_success), Toast.LENGTH_SHORT).show();
                        Common.setFragment(ProductFragment.getInstance(), R.id.frame_layout_home,
                                getActivity().getSupportFragmentManager());
                        Common.dismissSnackBar(getActivity(), mScrollView, "Upload product finished 100.0 %");
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.CODE_REQUEST_GALLERY)
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();
                mImgAddproduct.setImageURI(mFileUri);
            }
    }

    private void restData() {
        mInputProductName.setText("");
        mInputProductPrice.setText("");
        mInputProductDescription.setText("");
        mFileUri = null;
        mImgAddproduct.setImageBitmap(null);
    }

}