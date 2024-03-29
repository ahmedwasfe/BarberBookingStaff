package com.ahmet.barberbookingstaff.ui.subActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.ui.price.TotalPriceFragment;
import com.ahmet.barberbookingstaff.callback.IServicesListener;
import com.ahmet.barberbookingstaff.callback.IShoppingItemSelectedListener;
import com.ahmet.barberbookingstaff.model.Services;
import com.ahmet.barberbookingstaff.model.CartItem;
import com.ahmet.barberbookingstaff.model.EventBus.DismissFromBottomSheetEvent;
import com.ahmet.barberbookingstaff.model.Products;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.ui.product.ProductsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class DoneServicsesActivity extends AppCompatActivity implements
        IServicesListener, IShoppingItemSelectedListener {

    @BindView(R.id.txt_customer_name)
    TextView mTxtCustomerName;
    @BindView(R.id.txt_customer_phone)
    TextView mTxtCustomerPhone;
    @BindView(R.id.chip_group_service)
    ChipGroup mChipGroupService;
    @BindView(R.id.chip_group_shopping)
    ChipGroup mChipGroupShopping;
    @BindView(R.id.txt_complete_service)
    AppCompatAutoCompleteTextView mTxtCompleteService;
    @BindView(R.id.img_customer_selected)
    ImageView mImgCustomerSelected;
    @BindView(R.id.img_add_shopping)
    ImageView mImgAddShopping;
    @BindView(R.id.btn_finish)
    Button mBtnFinish;
    @BindView(R.id.radio_no_picture)
    RadioButton mRadioNoPicture;
    @BindView(R.id.radio_picture)
    RadioButton mRadioPicture;

    private AlertDialog mDialog;

    private IServicesListener iServicesListener;

    private StorageReference mStorageReference;

    private HashSet<Services> mHashServicesAdded;
  // private List<Products> mListShopping;

    private LayoutInflater inflater;

    private Uri fileUri;

    private static final int CODE_REQUEST_CAMERA = 100;
    private static final int CODE_REQUEST_GALLERY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_servicses);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.checkout);

        ButterKnife.bind(this);

        init();

        initView();

        setCustomerInformation();

        loadBarberServices();
    }

    private void initView() {

        mRadioPicture.setOnCheckedChangeListener((compoundButton, b) -> {

            if (b){

                mImgCustomerSelected.setVisibility(View.VISIBLE);
                mBtnFinish.setEnabled(false);
            }
        });

        mRadioNoPicture.setOnCheckedChangeListener((compoundButton, b) -> {

            if (b){

                mImgCustomerSelected.setVisibility(View.GONE);
                mBtnFinish.setEnabled(true);
            }
        });

        mBtnFinish.setOnClickListener(view -> {

            if (mRadioNoPicture.isChecked()){

                mDialog.dismiss();

                TotalPriceFragment totalPriceFragment = TotalPriceFragment.getInstance();
                Bundle bundle = new Bundle();
                bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(mHashServicesAdded));
               // bundle.putString(Common.SHOPPING_ITEMS, new Gson().toJson(mListShopping));
                totalPriceFragment.setArguments(bundle);
                totalPriceFragment.show(getSupportFragmentManager(), Common.TAG_PRICE);

            } else
                uploadPicture(fileUri);
        });

        mImgCustomerSelected.setOnClickListener(view -> showBottomSheetToSelectImage());

        mImgAddShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProductsFragment mShoppingFragment = ProductsFragment.getInstance(DoneServicsesActivity.this);
                mShoppingFragment.show(getSupportFragmentManager(), Common.TAG_PRODUCTS);
            }
        });
    }

    private void uploadPicture(Uri fileUri) {

        mDialog.show();

        if (fileUri != null){

            String fileName = Common.getFileName(getContentResolver(), fileUri);
            String path = new StringBuilder("Customer_Pictures/")
                    .append(fileName)
                    .toString();

            mStorageReference = FirebaseStorage.getInstance().getReference(path);
            UploadTask uploadTask = mStorageReference.putFile(fileUri);
                    Task<Uri> taskUri = uploadTask.continueWithTask(task -> {

                        if (!task.isSuccessful())
                            Toast.makeText(DoneServicsesActivity.this, getString(R.string.can_not_upload_image), Toast.LENGTH_SHORT).show();

                        return mStorageReference.getDownloadUrl();
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()){

                                String imageUri = task.getResult().toString()
                                        .substring(0, task.getResult().toString().indexOf("&token"));
                                Log.d("DOWNLIADABLELINK", imageUri);

                                mDialog.dismiss();

                                // generate data here and upliad to firebase firestore
                                TotalPriceFragment totalPriceFragment = TotalPriceFragment.getInstance();
                                Bundle bundle = new Bundle();
                                bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(mHashServicesAdded));
                              //  bundle.putString(Common.SHOPPING_ITEMS, new Gson().toJson(mListShopping));
                                bundle.putString(Common.IMAGE_DOWNLIADABLE_URL, imageUri);
                                totalPriceFragment.setArguments(bundle);
                                totalPriceFragment.show(getSupportFragmentManager(), Common.TAG_PRICE);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        mDialog.dismiss();
                        Toast.makeText(DoneServicsesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });


        } else {

            Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void showBottomSheetToSelectImage() {

        BottomSheetDialog sheetDialogSelectImage = new BottomSheetDialog(DoneServicsesActivity.this);
        sheetDialogSelectImage.setTitle(R.string.select_Image);
        sheetDialogSelectImage.setCancelable(false);
        sheetDialogSelectImage.setCanceledOnTouchOutside(true);

        View sheetLayout = inflater.inflate(R.layout.sheet_dialog_select_image, null);

        ImageView imgSelectImageFromGallery = sheetLayout.findViewById(R.id.img_select_image_from_gallery);
        ImageView imgSelectImageFromCamera = sheetLayout.findViewById(R.id.img_select_image_from_camera);

        imgSelectImageFromCamera.setOnClickListener(view -> {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            fileUri = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, CODE_REQUEST_CAMERA);

            sheetDialogSelectImage.dismiss();
        });

        imgSelectImageFromGallery.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            fileUri = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.setType("image/*");
            startActivityForResult(intent, CODE_REQUEST_GALLERY);

            sheetDialogSelectImage.dismiss();
        });

        sheetDialogSelectImage.setContentView(sheetLayout);
        sheetDialogSelectImage.show();
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

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage(R.string.please_wait)
                .build();

        inflater = LayoutInflater.from(this);

        iServicesListener = this;

        mHashServicesAdded = new HashSet<>();
      //  mListShopping = new ArrayList<>();
    }

    private void loadBarberServices() {

        mDialog.show();

        FirebaseFirestore.getInstance()
                .collection(Common.KEY_AllSALON_REFERANCE)
                .document(Common.currentSalon.getSalonId())
                .collection(Common.KEY_SERICES_REFERANCE)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        List<Services> mListServices = new ArrayList<>();

                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            Services services = documentSnapshot.toObject(Services.class);
                            mListServices.add(services);
                        }

                        iServicesListener.onLoadServicesSuccess(mListServices);
                    }

                }).addOnFailureListener(e -> iServicesListener.onLoadServicesFailed(e.getMessage()));



    }

    private void setCustomerInformation() {

        mTxtCustomerName.setText(Common.currentBooking.getCustomerName());
        mTxtCustomerPhone.setText(Common.currentBooking.getCustomerPhone());
    }

    @Override
    public void onLoadServicesSuccess(List<Services> mListServices) {

        List<String> mListNameServices = new ArrayList<>();
        // Sort alpha-bet
        Collections.sort(mListServices, (services, t1) -> services.getServiceName().compareTo(t1.getServiceName()));

        // Add all name of services after sort
        for (Services services : mListServices)
            mListNameServices.add(services.getServiceName());

        ArrayAdapter<String> mNameServicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, mListNameServices);
        // Will start working from forst character
        mTxtCompleteService.setThreshold(1);
        mTxtCompleteService.setAdapter(mNameServicesAdapter);
        mTxtCompleteService.setOnItemClickListener((adapterView, view, i, l) -> {

            // add to chip group
            int index = mListNameServices.indexOf(mTxtCompleteService.getText().toString().trim());

            if (!mHashServicesAdded.contains(mListServices.get(index))) {

                // We dont want to have dublicate service in list so we use Hashset
                mHashServicesAdded.add(mListServices.get(index));

                Chip chip = (Chip) inflater.inflate(R.layout.raw_chip, null);
                chip.setText(mTxtCompleteService.getText().toString());
                chip.setTag(i);
                mTxtCompleteService.setText("");

                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mChipGroupService.removeView(view);
                        mHashServicesAdded.remove(chip.getTag());
                    }
                });

                mChipGroupService.addView(chip);

            } else {

                mTxtCompleteService.setText("");
            }
        });

      //  mDialog.dismiss();
        loadExtraItems();

    }

    @Override
    public void onLoadServicesFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(Products products) {

        // Here we will create an list to hold products items
       // mListShopping.add(products);
       // Log.d("SHOPPINGITEM", "" + mListShopping.size());

        // Create a new Cart item
        CartItem cartItem = new CartItem();
        cartItem.setProductId(products.getId());
        cartItem.setProductName(products.getName());
        cartItem.setProductPrice(products.getPrice());
        cartItem.setProductImage(products.getImage());
        cartItem.setProductQuantity(1);
        cartItem.setUserPhone(Common.currentBooking.getCustomerPhone());

        // If user submit with empty cart
        if (Common.currentBooking.getmListCartItem() == null)
            Common.currentBooking.setmListCartItem(new ArrayList<CartItem>());

        // we will use this flag to update cart item quantity by 1
        boolean flag = false;
        // If already have item with same name in cart
        for (int x = 0; x < Common.currentBooking.getmListCartItem().size(); x++){

            if (Common.currentBooking.getmListCartItem().get(x).getProductName()
                        .equals(products.getName())){
                // Enable falg
                flag = true;
                CartItem updateCartItem = Common.currentBooking.getmListCartItem().get(x);
                updateCartItem.setProductQuantity(updateCartItem.getProductQuantity() + 1);
                // Update list
                Common.currentBooking.getmListCartItem().set(x, updateCartItem);
            }
        }

        // if flag == false -> new item addedd
        if (!flag){
            Common.currentBooking.getmListCartItem().add(cartItem);

            Chip chip = (Chip) inflater.inflate(R.layout.raw_chip, null);
            chip.setText(cartItem.getProductName());
            chip.setTag(Common.currentBooking.getmListCartItem().indexOf(cartItem));
           // mTxtCompleteService.setText("");

            chip.setOnCloseIconClickListener(view -> {
                mChipGroupShopping.removeView(view);
                Common.currentBooking.getmListCartItem().remove(chip.getTag());
            });
            mChipGroupShopping.addView(chip);

          // falg = true - update items
        } else{

            mChipGroupShopping.removeAllViews();
            loadExtraItems();

        }

    }

    private void loadExtraItems() {

        if (Common.currentBooking.getmListCartItem() != null){

            for (CartItem cartItem : Common.currentBooking.getmListCartItem()){

                Chip chip = (Chip) inflater.inflate(R.layout.raw_chip, null);
                chip.setText(new StringBuilder(cartItem.getProductName())
                                .append(" x")
                                .append(cartItem.getProductQuantity()));
                chip.setTag(Common.currentBooking.getmListCartItem().indexOf(cartItem));
               // mTxtCompleteService.setText("");

                chip.setOnCloseIconClickListener(view -> {
                    mChipGroupShopping.removeView(view);
                    Common.currentBooking.getmListCartItem().remove(chip.getTag());
                });
                mChipGroupShopping.addView(chip);

            }
        }
        mDialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_REQUEST_CAMERA){
            if (resultCode == RESULT_OK){

                Bitmap bitmap = null;
                ExifInterface exifInterface = null;

                try{

                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                    exifInterface = new ExifInterface(getContentResolver().openInputStream(fileUri));
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotateitmap = null;

                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateitmap = rotateImage(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateitmap = rotateImage(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateitmap = rotateImage(bitmap, 27);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                            default:
                                rotateitmap = bitmap;
                            break;
                    }

                    mImgCustomerSelected.setImageBitmap(rotateitmap);
                    mBtnFinish.setEnabled(true);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else if (requestCode == CODE_REQUEST_GALLERY){

            if (resultCode == RESULT_OK){

                try {
                    fileUri = data.getData();
                    InputStream imageInputStream = getContentResolver().openInputStream(fileUri);
                    Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageInputStream);
                    mImgCustomerSelected.setImageBitmap(selectedImageBitmap);
                    mBtnFinish.setEnabled(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, int degreRotate) {

        Matrix matrix = new Matrix();
        matrix.postRotate(degreRotate);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /*
    @Override
    public void onDismissSheetDialog(boolean fromButton) {
        // If equal true
        if (fromButton)
            finish();
    }
    */

    // Convert to Event Bus

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void dismissDialog(DismissFromBottomSheetEvent event){

        if (event.isButtonClick())
            finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
