package com.ahmet.barberbookingstaff;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Fragments.ShoppingFragment;
import com.ahmet.barberbookingstaff.Fragments.TotalPriceFragment;
import com.ahmet.barberbookingstaff.Interface.IBarberServicesListener;
import com.ahmet.barberbookingstaff.Interface.ISheetDialogDismissListener;
import com.ahmet.barberbookingstaff.Interface.IShoppingItemSelectedListener;
import com.ahmet.barberbookingstaff.Model.BarberServices;
import com.ahmet.barberbookingstaff.Model.Shopping;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class DoneServicsesActivity extends AppCompatActivity implements IBarberServicesListener, IShoppingItemSelectedListener, ISheetDialogDismissListener {

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

    private IBarberServicesListener iBarberServicesListener;

    private StorageReference mStorageReference;

    private HashSet<BarberServices> mHashServicesAdded;
    private List<Shopping> mListShopping;

    private LayoutInflater inflater;

    private Uri fileUri;
    private static final int CODE_REQUEST_CAMERA = 100;
    private static final int CODE_REQUEST_GALLERY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_servicses);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Checkout");

        ButterKnife.bind(this);

        init();

        initView();

        setCustomerInformation();

        loadBarberServices();
    }

    private void initView() {

        mRadioPicture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){

                    mImgCustomerSelected.setVisibility(View.VISIBLE);
                    mBtnFinish.setEnabled(false);
                }
            }
        });

        mRadioNoPicture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){

                    mImgCustomerSelected.setVisibility(View.GONE);
                    mBtnFinish.setEnabled(true);
                }
            }
        });

        mBtnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mRadioNoPicture.isChecked()){

                    mDialog.dismiss();

                    TotalPriceFragment totalPriceFragment = TotalPriceFragment.getInstance(DoneServicsesActivity.this);
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(mHashServicesAdded));
                    bundle.putString(Common.SHOPPING_ITEMS, new Gson().toJson(mListShopping));
                    totalPriceFragment.setArguments(bundle);
                    totalPriceFragment.show(getSupportFragmentManager(), "Price");

                } else
                    uploadPicture(fileUri);
            }
        });

        mImgCustomerSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               showBottomSheetToSelectImage();

            }
        });

        mImgAddShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShoppingFragment mShoppingFragment = ShoppingFragment.getInstance(DoneServicsesActivity.this);
                mShoppingFragment.show(getSupportFragmentManager(), "Shopping");
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
                    Task<Uri> taskUri = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                            if (!task.isSuccessful())
                                Toast.makeText(DoneServicsesActivity.this, "Can,t upload image please try again", Toast.LENGTH_SHORT).show();

                            return mStorageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()){

                                String imageUri = task.getResult().toString()
                                        .substring(0, task.getResult().toString().indexOf("&token"));
                                Log.d("DOWNLIADABLELINK", imageUri);

                                mDialog.dismiss();

                                // generate data here and upliad to firebase firestore
                                TotalPriceFragment totalPriceFragment = TotalPriceFragment.getInstance(DoneServicsesActivity.this);
                                Bundle bundle = new Bundle();
                                bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(mHashServicesAdded));
                                bundle.putString(Common.SHOPPING_ITEMS, new Gson().toJson(mListShopping));
                                bundle.putString(Common.IMAGE_DOWNLIADABLE_URL, imageUri);
                                totalPriceFragment.setArguments(bundle);
                                totalPriceFragment.show(getSupportFragmentManager(), "Price");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(DoneServicsesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        } else {

            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBottomSheetToSelectImage() {

        BottomSheetDialog sheetDialogSelectImage = new BottomSheetDialog(DoneServicsesActivity.this);
        sheetDialogSelectImage.setTitle("Select Image");
        sheetDialogSelectImage.setCancelable(false);
        sheetDialogSelectImage.setCanceledOnTouchOutside(true);

        View sheetLayout = inflater.inflate(R.layout.sheet_dialog_select_image, null);

        ImageView imgSelectImageFromGallery = sheetLayout.findViewById(R.id.img_select_image_from_gallery);
        ImageView imgSelectImageFromCamera = sheetLayout.findViewById(R.id.img_select_image_from_camera);

        imgSelectImageFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                fileUri = getOutputMediaFileUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CODE_REQUEST_CAMERA);

                sheetDialogSelectImage.dismiss();
            }
        });

        imgSelectImageFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                fileUri = getOutputMediaFileUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.setType("image/*");
                startActivityForResult(intent, CODE_REQUEST_GALLERY);

                sheetDialogSelectImage.dismiss();
            }
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
                .build();

        inflater = LayoutInflater.from(this);

        iBarberServicesListener = this;

        mHashServicesAdded = new HashSet<>();
        mListShopping = new ArrayList<>();
    }

    private void loadBarberServices() {

        mDialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.cityName)
                .collection("Branch")
                .document(Common.currentSalon.getSalonID())
                .collection("Services")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            List<BarberServices> mListBarberServices = new ArrayList<>();

                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                BarberServices barberServices = documentSnapshot.toObject(BarberServices.class);
                                mListBarberServices.add(barberServices);
                            }

                            iBarberServicesListener.onLoadBarberServicesSuccess(mListBarberServices);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                iBarberServicesListener.onLoadBarberServicesFailed(e.getMessage());

            }
        });



    }

    private void setCustomerInformation() {

        mTxtCustomerName.setText(Common.currentBooking.getCustomerName());
        mTxtCustomerPhone.setText(Common.currentBooking.getCustomerPhone());
    }

    @Override
    public void onLoadBarberServicesSuccess(List<BarberServices> mListBarberServices) {

        List<String> mListNameServices = new ArrayList<>();
        // Sort alpha-bet
        Collections.sort(mListBarberServices, new Comparator<BarberServices>() {
            @Override
            public int compare(BarberServices barberServices, BarberServices t1) {
                return barberServices.getServiceName().compareTo(t1.getServiceName());
            }
        });

        // Add all name of services after sort
        for (BarberServices barberServices : mListBarberServices)
            mListNameServices.add(barberServices.getServiceName());

        ArrayAdapter<String> mNameServicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, mListNameServices);
        // Will start working from forst character
        mTxtCompleteService.setThreshold(1);
        mTxtCompleteService.setAdapter(mNameServicesAdapter);
        mTxtCompleteService.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // add to chip group
                int index = mListNameServices.indexOf(mTxtCompleteService.getText().toString().trim());

                if (!mHashServicesAdded.contains(mListBarberServices.get(index))) {

                    // We dont want to have dublicate service in list so we use Hashset
                    mHashServicesAdded.add(mListBarberServices.get(index));

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
            }

        });

        mDialog.dismiss();

    }

    @Override
    public void onLoadBarberServicesFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(Shopping shopping) {

        // Here we will create an list to hold shopping items
        mListShopping.add(shopping);
        Log.d("SHOPPINGITEM", "" + mListShopping.size());

        Chip chip = (Chip) inflater.inflate(R.layout.raw_chip, null);
        chip.setText(shopping.getName());
        chip.setTag(mListShopping.indexOf(shopping));
        mTxtCompleteService.setText("");

        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChipGroupShopping.removeView(view);
                mListShopping.remove(chip.getTag());
            }
        });

        mChipGroupShopping.addView(chip);
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

    @Override
    public void onDismissSheetDialog(boolean fromButton) {
        // If equal true
        if (fromButton)
            finish();
    }
}
