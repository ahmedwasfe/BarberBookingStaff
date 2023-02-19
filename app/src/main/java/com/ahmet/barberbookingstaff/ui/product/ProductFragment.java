package com.ahmet.barberbookingstaff.ui.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.adapter.ProductsAdapter;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.model.Products;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ahmed.com.swiperecyclerview.MButton;
import ahmed.com.swiperecyclerview.SwipeRecyclerViewHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.supercharge.shimmerlayout.ShimmerLayout;

import static android.app.Activity.RESULT_OK;

public class ProductFragment extends Fragment {


    @BindView(R.id.recycler_products)
    RecyclerView mRecyclerProducts;
    @BindView(R.id.shimmer_layout_products)
    ShimmerLayout mShimmerLayout;
    @BindView(R.id.linear_no_product)
    LinearLayout mLinearNoProduct;
    @BindView(R.id.text_no_product)
    TextView mTxtNoProduct;

    private ImageView mImgProduct;

    private ProductViewModel productViewModel;
    private ProductsAdapter productsAdapter;

    private Uri mFileUri = null;

    private android.app.AlertDialog mDialog;

    private static ProductFragment instance;
    public static ProductFragment getInstance(){
        return instance == null ? new ProductFragment() : instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick(R.id.fab_add_products) void onAddProductClick(){
        Common.setFragment(AddProductFragment.getInstance(), R.id.frame_layout_home,
                getActivity().getSupportFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        View layoutView = inflater.inflate(R.layout.fragment_show_products, container, false);

       ButterKnife.bind(this, layoutView);

        initViews();

        productViewModel.getMessageError()
                .observe(getViewLifecycleOwner(), error -> {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                });

        productViewModel.getListMutableProducts()
                .observe(getViewLifecycleOwner(), products -> {

                    mShimmerLayout.stopShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);

                    productsAdapter = new ProductsAdapter(getActivity(), products);
                    mRecyclerProducts.setAdapter(productsAdapter);
                });

        mLinearNoProduct.setVisibility(View.GONE);

        return layoutView;
    }


    private void initViews() {

        mShimmerLayout.startShimmerAnimation();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerProducts.setHasFixedSize(true);

        mRecyclerProducts.setLayoutManager(layoutManager);
        mRecyclerProducts.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        SwipeRecyclerViewHelper recyclerViewHelper = new SwipeRecyclerViewHelper(
                getActivity(), mRecyclerProducts, width/6) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(),getString(R.string.delete),24,0,
                        getActivity().getColor(R.color.colorRed), position -> {

                    Products products = productsAdapter.getItemAtPosition(position);
                    showDilogDeleteProduct(products);

                }));

                mListMButton.add(new MButton(getActivity(),getString(R.string.update),24,0,
                        getActivity().getColor(R.color.colorGreen), position -> {

                    Products product = productsAdapter.getItemAtPosition(position);
                    showDilogUpdateProduct(product);

                }));
            }
        };

        mDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(getActivity())
                .build();
    }

    private void showDilogUpdateProduct(Products products) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_product, null);
        builder.setView(layoutView);

        EditText mInputName = layoutView.findViewById(R.id.input_update_product_name);
        EditText mInputPrice = layoutView.findViewById(R.id.input_update_product_price);
        EditText mInputDescription = layoutView.findViewById(R.id.input_update_product_description);
        mImgProduct = layoutView.findViewById(R.id.img_update_product);

        mInputName.setText(products.getName());
        mInputPrice.setText(String.valueOf(products.getPrice()));
        mInputDescription.setText(products.getDescription());
        Picasso.get().load(products.getImage()).into(mImgProduct);

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);

        mImgProduct.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    StrictMode.VmPolicy.Builder builder1 = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder1.build());

                    mFileUri = Common.getOutputMediaFileUri();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
                    intent.setType("image/*");
                    startActivityForResult(intent, Common.CODE_REQUEST_GALLERY);
                });

        layoutView.findViewById(R.id.btn_update_product)
                .setOnClickListener(v -> {

                    String name = mInputName.getText().toString();
                    String price = mInputPrice.getText().toString();
                    String description = mInputDescription.getText().toString();

                    Products product = products;
                    product.setName(name);
                    product.setPrice(TextUtils.isEmpty(price) ? 0 : Long.parseLong(price));
                    product.setDescription(description);

                    Map<String, Object> mapUpdateProduct = new HashMap<>();
                    mapUpdateProduct.put("name", product.getName());
                    mapUpdateProduct.put("price", product.getPrice());
                    mapUpdateProduct.put("description", product.getDescription());

                    if (mFileUri != null)
                        uploadImage(mapUpdateProduct, products.getId());
                    else
                        updateProduct(mapUpdateProduct, products.getId());

                    dialog.dismiss();
                });

        layoutView.findViewById(R.id.btn_cancel_update_product)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();

    }

    private void updateProduct(Map<String, Object> product, String id) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_AllSALON_REFERANCE)
                .child(Common.currentSalon.getSalonId())
                .child(Common.KEY_PRODUCTS_REFERANCE)
                .child(id)
                .updateChildren(product)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        productViewModel.loadAllProduct();
                        Toast.makeText(getActivity(), getString(R.string.update_product_success), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage(Map<String, Object> product, String id) {

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
                        product.put("image", uri.toString());
                        updateProduct(product, id);
                    });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done) + " ").append(progress).append("%"));
        });
    }

    private void showDilogDeleteProduct(Products products) {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_product)
                .setMessage(R.string.confirm_delete_product)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).setPositiveButton(R.string.delete, (dialog, which) -> {

            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_AllSALON_REFERANCE)
                    .child(Common.currentSalon.getSalonId())
                    .child(Common.KEY_PRODUCTS_REFERANCE)
                    .child(products.getId())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        productViewModel.loadAllProduct();
                        Toast.makeText(getActivity(), getString(R.string.delete_product_success), Toast.LENGTH_SHORT).show();
                    });
        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_GALLERY){
            if (resultCode == RESULT_OK){
                mFileUri = data.getData();
                mImgProduct.setImageURI(mFileUri);
            }
        }
    }
}