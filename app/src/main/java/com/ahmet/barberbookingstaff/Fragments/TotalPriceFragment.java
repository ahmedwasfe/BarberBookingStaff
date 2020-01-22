package com.ahmet.barberbookingstaff.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.Adapter.ShoppingConfirmAdapter;
import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Model.BarberServices;
import com.ahmet.barberbookingstaff.Model.CartItem;
import com.ahmet.barberbookingstaff.Model.EventBus.DismissFromBottomSheetEvent;
import com.ahmet.barberbookingstaff.Model.FCMResponse;
import com.ahmet.barberbookingstaff.Model.FCMSendData;
import com.ahmet.barberbookingstaff.Model.Invoice;
import com.ahmet.barberbookingstaff.Model.Notification;
import com.ahmet.barberbookingstaff.Model.Token;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.Retrofit.IFCMService;
import com.ahmet.barberbookingstaff.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TotalPriceFragment extends BottomSheetDialogFragment {

    private Unbinder mUnbinder;

    @BindView(R.id.text_salon_name)
    TextView mTxtSalonName;
    @BindView(R.id.text_barber_name)
    TextView mTxtBarberName;
    @BindView(R.id.text_customer_name)
    TextView mTxtCustomerName;
    @BindView(R.id.text_customer_phone)
    TextView mTxtCustomerPhone;
    @BindView(R.id.text_time_bookin)
    TextView mTxtTimeBooking;
    @BindView(R.id.txt_total_price)
    TextView mTxtTotalPrice;
    @BindView(R.id.chip_group_services)
    ChipGroup mChipGroup_Services;
    @BindView(R.id.recycler_items_shopping)
    RecyclerView mRecyclerShoopingItem;
    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    private HashSet<BarberServices> mHashServicesAdded;
   // private List<Products> mListShopping;

    private IFCMService mIFCMService;

    private AlertDialog mDialog;

    private String imageUrl;



    private static TotalPriceFragment instance;
    public static TotalPriceFragment getInstance(){

        return instance == null ? new TotalPriceFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_total_price, container, false);

        mUnbinder = ButterKnife.bind(this, layoutView);

        init();

        initView();

        getBundle(getArguments());

        setInformation();

        return layoutView;
    }

    private void initView() {

        mRecyclerShoopingItem.setHasFixedSize(true);
        mRecyclerShoopingItem.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));

        mBtnConfirm.setOnClickListener(view -> {

            mDialog.show();

            // /AllSalon/Gaza/Branch/AFXjgtlJwztf7cLFumNT/Barber/P06LozRgyEgFF5ehHPLZ/24_07_2019/19
            // Update booking information, set done = true
            DocumentReference mDocumentRef = FirebaseFirestore.getInstance()
                    .collection(Common.KEY_COLLECTION_AllSALON)
                    .document(Common.currentSalon.getSalonID())
                    .collection(Common.KEY_COLLECTION_BARBER)
                    .document(Common.currentBarber.getBarberID())
                    .collection(Common.mSimpleDateFormat.format(Common.bookingDate.getTime()))
                    .document(Common.currentBooking.getBookingID());


            mDocumentRef.get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()){

                            if (task.getResult().exists()){

                                // Update
                                Map<String, Object> mMapUpdateBooking = new HashMap<>();
                                mMapUpdateBooking.put("done", true);

                                mDocumentRef.update(mMapUpdateBooking)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mDialog.dismiss();
                                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            // If update done, create invoice
                                            createInvoice();
                                        }
                                    }
                                });
                            }
                        }

                    }).addOnFailureListener(e -> {
                        mDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage()
                                , Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void createInvoice() {

        // Create invoice
        CollectionReference mCollectionRefInvoice = FirebaseFirestore.getInstance()
                .collection(Common.KEY_COLLECTION_AllSALON)
                .document(Common.currentSalon.getSalonID())
                .collection(Common.KEY_COLLECTION_INVOICES);

        Invoice invoice = new Invoice();

        invoice.setSalonID(Common.currentSalon.getSalonID());
        invoice.setSalonName(Common.currentSalon.getName());
        invoice.setSalonAddress(Common.currentSalon.getAddress());

        invoice.setBarberID(Common.currentBarber.getBarberID());
        invoice.setBarberName(Common.currentBarber.getName());

        invoice.setCustomerName(Common.currentBooking.getCustomerName());
        invoice.setCustomerPhone(Common.currentBooking.getCustomerPhone());

        invoice.setImageUrl(imageUrl);
        invoice.setFinalPrice(calculatePrice());

        invoice.setmListBarberServices(new ArrayList<>(mHashServicesAdded));
        invoice.setmListShopping(Common.currentBooking.getmListCartItem());


        mCollectionRefInvoice.document()
                .set(invoice)
                .addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    sendNotificationUpdateToUser(Common.currentBooking.getCustomerPhone());
                }
            }
        });


    }

    private void sendNotificationUpdateToUser(String customerPhone) {


        // Create Notification
        Notification notification = new Notification();
        notification.setTitle(getString(R.string.finish_booking));
        notification.setContent(getString(R.string.thank_you_for_booking));
        notification.setUuid(UUID.randomUUID().toString());
        notification.setRead(false);
       // notification.setServerTimestamp(FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection(Common.KEY_COLLECTION_USER)
                .document(customerPhone)
                .collection(Common.KEY_COLLECTION_NOTIFICATIONS)
                .document(notification.getUuid())
                .set(notification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // get token of user first
                        FirebaseFirestore.getInstance()
                                .collection(Common.KEY_COLLECTION_TOKENS)
                                .whereEqualTo("userPhone", customerPhone)
                                .get()
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful() && task.getResult().size() > 0){

                                        Token token = new Token();
                                        for (DocumentSnapshot docSnapshot : task.getResult())
                                            token = docSnapshot.toObject(Token.class);

                                        // Create notification
                                        FCMSendData fcmSendData = new FCMSendData();

                                        Map<String, String> mMapSendData = new HashMap<>();
                                        mMapSendData.put("done", "true");

                                        // Information need for rating barber
                                        //  mMapSendData.put(Common.KEY_RATING_CITY, Common.cityName);
                                        mMapSendData.put(Common.KEY_RATING_SALON_ID, Common.currentSalon.getSalonID());
                                        mMapSendData.put(Common.KEY_RATING_SALON_NAME, Common.currentSalon.getName());
                                        mMapSendData.put(Common.KEY_RATING_BARBER_ID, Common.currentBarber.getBarberID());

                                        fcmSendData.setTo(token.getToken());
                                        fcmSendData.setmMapData(mMapSendData);

                                        mIFCMService.sendNotification(fcmSendData)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(Schedulers.newThread())
                                                .subscribe(fcmResponse -> {
                                                    mDialog.dismiss();
                                                    dismiss();
                                                    EventBus.getDefault().postSticky(new DismissFromBottomSheetEvent(true));
                                                    //mISheetDialogDismissListener.onDismissSheetDialog(true);
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {

                                                        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                });
                    }
                });

    }

    private void setInformation() {

        mTxtSalonName.setText(Common.currentSalon.getName());
        mTxtCustomerName.setText(Common.currentBooking.getCustomerName());
        mTxtBarberName.setText(Common.currentBarber.getName());
        mTxtTimeBooking.setText(Common.convertTimeSoltToString(Common.currentBooking.getTimeSlot().intValue()));
        mTxtCustomerPhone.setText(Common.currentBooking.getCustomerPhone());

        if (mHashServicesAdded.size() > 0){

            // Add yo chip
            int tag = 0;
            for (BarberServices barberServices : mHashServicesAdded){

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.raw_chip, null);
                chip.setText(barberServices.getServiceName());
                chip.setTag(tag);

                chip.setOnCloseIconClickListener(view -> {
                    mHashServicesAdded.remove(view.getTag());
                    mChipGroup_Services.removeView(view);

                    calculatePrice();
                });

                mChipGroup_Services.addView(chip);

                tag ++;
            }

        }

        if (Common.currentBooking.getmListCartItem() != null) {

            if (Common.currentBooking.getmListCartItem().size() > 0) {

                ShoppingConfirmAdapter mShoppingConfirmAdapter = new ShoppingConfirmAdapter(getActivity(),
                        Common.currentBooking.getmListCartItem());
                mRecyclerShoopingItem.setAdapter(mShoppingConfirmAdapter);
            }

            calculatePrice();
        }
    }

    private double calculatePrice() {

        double price = Common.DEFAULT_PRICE;

        for (BarberServices barberServices : mHashServicesAdded)
            price += barberServices.getServicePrice();

        if (Common.currentBooking.getmListCartItem() != null){
            for (CartItem cartItem : Common.currentBooking.getmListCartItem())
                price += (cartItem.getProductPrice() * cartItem.getProductQuantity());
        }

        mTxtTotalPrice.setText(new StringBuilder(Common.MONEY_SIGN)
                        .append(price));

        return price;

    }

    private void getBundle(Bundle arguments) {

        this.mHashServicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());

//        this.mListShopping = new Gson()
//                .fromJson(arguments.getString(Common.SHOPPING_ITEMS),
//                        new TypeToken<List<Products>>(){}.getType());

        imageUrl = arguments.getString(Common.IMAGE_DOWNLIADABLE_URL);
    }

    private void init() {

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setCancelable(false)
                .setMessage(R.string.please_wait)
                .build();

        mIFCMService = RetrofitClient.getInstance()
                .create(IFCMService.class);


    }
}