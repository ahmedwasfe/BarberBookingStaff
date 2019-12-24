package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Model.Products;
import com.ahmet.barberbookingstaff.R;
import com.github.ybq.android.spinkit.style.Circle;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class AddProductFragment extends Fragment {

    private Unbinder mUnbinder;

    @BindView(R.id.img_add_product)
    ImageView mImgAddproduct;
    @BindView(R.id.txt_input_product_name)
    TextInputEditText mInputProductName;
    @BindView(R.id.txt_input_product_price)
    TextInputEditText mInputProductPrice;
    @BindView(R.id.txt_input_product_description)
    TextInputEditText mInputProductDescription;
    @BindView(R.id.btn_add_product)
    Button mBtnAddProduct;
    @BindView(R.id.progress_load_image)
    ProgressBar mProgressBar;

    private Uri mFileUri;
    private String imageUri = "";

    private StorageReference mStorageRef;

    private AlertDialog mDialog;


    private static final int CODE_REQUEST_GALLERY = 108;

    @OnClick(R.id.btn_add_product)
    void submitProduct(){

        String name = mInputProductName.getText().toString();
        long price = Integer.parseInt(mInputProductPrice.getText().toString());
        String descriptopion = mInputProductDescription.getText().toString();

        uploadImage(name, price, descriptopion);
       // addProductsToShopping(name, price, descriptopion);

    }

    private void uploadImage(String name, long price, String descriptopion) {

       // mDialog.show();
        mBtnAddProduct.setEnabled(false);
        mBtnAddProduct.setText("");
        mProgressBar.setVisibility(View.VISIBLE);

        if (mFileUri != null){

            String filaName = Common.getFileName(getActivity().getContentResolver(), mFileUri);
            String filePath = new StringBuilder("Products_Pictures/")
                    .append(filaName)
                    .toString();

            mStorageRef = FirebaseStorage.getInstance().getReference().child(filePath);
            UploadTask uploadTask = mStorageRef.putFile(mFileUri);
            Task<Uri> taskUri = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful())
                        Toast.makeText(getActivity(), "Can,t upload image please try again", Toast.LENGTH_SHORT).show();
                    return mStorageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){

                         imageUri = task.getResult().toString();
                        Log.i("DOWNLIADABLELINK", imageUri);

                       // mDialog.dismiss();
                        mBtnAddProduct.setEnabled(true);
                        mBtnAddProduct.setText("Add Product");
                       // mBtnAddProduct.setBackgroundColor(R.color.colorAccent);
                        mProgressBar.setVisibility(View.GONE);

                        Products setProducts = new Products(name, imageUri, price);
                        FirebaseFirestore.getInstance().collection("AllSalon")
                                .document(Common.currentSalon.getSalonID())
                                .collection("Products")
                                .add(setProducts)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()){
                                           // mDialog.dismiss();
                                            mBtnAddProduct.setEnabled(true);
                                            mBtnAddProduct.setText("Add Product");
                                            mProgressBar.setVisibility(View.GONE);
                                            Toast.makeText(getActivity(), "Uploading success", Toast.LENGTH_SHORT).show();
                                            Common.setFragment(new ShowProductFragment(), R.id.frame_layout_product,
                                                    getActivity().getSupportFragmentManager());
                                            Products product = new Products();
                                            product.setId(task.getResult().getId());

                                            addProductsToShopping(name, price, descriptopion, imageUri,
                                                    Common.currentSalon.getSalonID(), Common.currentSalon.getName());
                                        }
                                    }
                                });
                    }
                }
            });
        }
    }

    private void addProductsToShopping(String name, Long price, String description, String image, String salonID, String salonName) {

      //  mDialog.show();


        Map<String, Object> mMapaddProducts = new HashMap<>();
        mMapaddProducts.put("name", name);
        mMapaddProducts.put("price", price);
        mMapaddProducts.put("description", description);
        mMapaddProducts.put("salonId", salonID);
        mMapaddProducts.put("salonName", salonName);
        mMapaddProducts.put("image", image);

        FirebaseFirestore.getInstance().collection("Shopping")
                .add(mMapaddProducts)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getActivity(), "Addedd to Shopping Success", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @OnClick(R.id.img_add_product)
    void selectImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mFileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        intent.setType("image/*");
        startActivityForResult(intent, CODE_REQUEST_GALLERY);
    }


    private static AddProductFragment instance;
    public static AddProductFragment getInstance(){

        if (instance == null){
            instance = new AddProductFragment();
        }
        return instance;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Barber Booking Staff");
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdir())
                return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" +
                timeStamp + "_" + new Random().nextInt() + ".jpg");

        return mediaFile;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_add_product, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();

        return layoutView;
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setCancelable(false)
                .setMessage("Loading Product...")
                .build();

        Circle circle = new Circle();
        mProgressBar.setIndeterminateDrawable(circle);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_REQUEST_GALLERY)
            if (resultCode == RESULT_OK){
                try {
                    mFileUri = data.getData();
                    InputStream imageInputStream = getActivity().getContentResolver().openInputStream(mFileUri);
                    Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageInputStream);
                    mImgAddproduct.setImageBitmap(selectedImageBitmap);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }

            }
    }

}
