package com.ahmet.barberbookingstaff.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ahmet.barberbookingstaff.api.IBarbersAPI;
import com.ahmet.barberbookingstaff.api.BarbersClient;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.BookingInformation;
import com.ahmet.barberbookingstaff.model.Products;
import com.ahmet.barberbookingstaff.model.Salon;
import com.ahmet.barberbookingstaff.model.Token;
import com.ahmet.barberbookingstaff.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Common {
    private static final String TAG = Common.class.getSimpleName();


    private static String BASE_URL = "https://oceanforit.000webhostapp.com/barbersbooking/";

    public static final String KEY_LOGGED = "LOGGED";
    public static final String KEY_LOGGED_EMAIL = "LOGGED_EMAIL";
    public static final String KEY_SALON_SELECTED = "Salon_Selected";
    public static String IS_LOGIN = "IsLogin";
    public static final String KEY_CITY = "CITY";
    public static final String KEY_SALON = "SALON";
    public static final String KEY_BARBER = "BARBER";

    public static final String KEY_IMAGE_DEFAULT = "https://i.postimg.cc/8CJzX9vq/hairdresser.png";

    // Notification
    public static final String KEY_NOTFI_TITLE = "title";
    public static final String KEY_NOTFI_CONTENT = "content";

    // sign Up
    public static final String KEY_EMAIL_SIGNUP = "email";
    public static final String KEY_FULLNAME_SIGNUP = "fullname";
    public static final String KEY_USERNAME_SIGNUP = "username";
    public static final String KEY_PASSWORD_SIGNUP = "password";
    public static final String KEY_GENDER_SIGNUP = "gender";
    public static final String KEY_PHONE_SIGNUP = "phone";
    public static final String KEY_WAHT_TO_DO = "wahttodo";
    public static final String KEY_UPDATE_PASSWORD = "updatepassword";

    public static final String KEY_RATING_CITY = "RATING_CITY";
    public static final String KEY_RATING_SALON_ID = "RATING_SALON_ID";
    public static final String KEY_RATING_SALON_NAME = "RATING_SALON_NAME";
    public static final String KEY_RATING_BARBER_ID = "RATING_BARBER_ID";

    public static final String SERVICES_ADDED = "SERVICES_ADDED";
    public static final String SHOPPING_ITEMS = "SHOPPING_ITEMS";
    public static final String MONEY_SIGN = "$ ";
    public static final String IMAGE_DOWNLIADABLE_URL = "DOWNLIADABLE_URL";

    public static final String KEY_IMAGES_PRODUCT_PATH = "ProductsPictures/";


    // TAGs
    public static final String TAG_TOKEN = "TOKEN";
    public static final String TAG_PRICE = "Price";
    public static final String TAG_PRODUCTS = "Products";

    public static String salonCity = "";

    // Public Tag Firebase Collections
    public static final String KEY_COLLECTION_USER = "User";
    public static final String KEY_AllSALON_REFERANCE = "AllSalon";
    public static final String KEY_COLLECTION_CENTERS = "Centers";
    public static final String KEY_BARBER_REFERANCE = "Barber";
    public static final String KEY_COLLECTION_BOOKING = "Booking";
    public static final String KEY_PRODUCTS_REFERANCE = "Products";
    public static final String KEY_COLLECTION_SHOPPING = "Shopping";
    public static final String KEY_NOTIFICATIONS_REFERANCE = "Notifications";
    public static final String KEY_TOKENS_REFERANCE = "Tokens";
    public static final String KEY_SERICES_REFERANCE = "Services";
    public static final String KEY_COLLECTION_INVOICES = "Invoices";

    // Public Tag Firebase Field
    public static final String KEY_FIELD_BARBER_TYPE = "barberType";
    public static final String KEY_FIELD_BARBER_NAME = "name";
    public static final String KEY_FIELD_BARBER_USERNAME = "username";
    public static final String KEY_FIELD_BARBER_PASSWORD = "password";

    // Public Tag Firebase Values
    public static final String KEY_VALUE_BARBER_TYPE = "Admin";

    // Storag
    public static final String KEY_IMAGES_PROFILE_PATH = "ProfileImages/";

    public static String cityName = "";
    public static Salon currentSalon;
    public static Barber currentBarber;
    public static BookingInformation currentBooking;
    public static Products currentProduct;

    public static int setp = 0; // init first setp is 0

    public static final int TIME_SLOT_TOTAL = 20;
    public static final int MAX_NOTIFICATIONS_PER_LOAD = 10;
    // default witout on services extra and items extra
    public static final double DEFAULT_PRICE = 30;

    public static final Object DISABLE_TAG = "DISABLE";

    public static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
    public static Calendar bookingDate = Calendar.getInstance();
    public static String email = "";
    public static int step = 0;

    // Code Request
    public static final int CODE_REQUEST_SIGNIN = 1880;
    public static final int CODE_REQUEST_GALLERY = 1881;
    public static final int CODE_REQUEST_CHANGE_IMAGE_BARBER = 1882;
    public static final int PERMISSION_REQUEST_CODE = 1883;

    public static IBarbersAPI getBarbersAPI(){
        return BarbersClient.getClient(BASE_URL).create(IBarbersAPI.class);
    }

    public static void getSalonName(IBarbersAPI barbersAPI, TextView textView, String email){

        barbersAPI.getSalonInfo(email)
                .enqueue(new Callback<Salon>() {
                    @Override
                    public void onResponse(Call<Salon> call, Response<Salon> response) {
                        if (response.isSuccessful()) {
                            textView.setText(response.body().getSalonName());
                            Log.d(TAG, "onResponse: " + response.body().getSalonName());
                        }
                    }

                    @Override
                    public void onFailure(Call<Salon> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    public static String convertTimeSoltToString(int solt) {

        switch (solt) {

            case 0:
                return "9:00 - 9:30";
            case 1:
                return "9:30 - 10:00";
            case 2:
                return "10:00 - 10:30";
            case 3:
                return "10:30 - 11:00";
            case 4:
                return "11:00 - 11:30";
            case 5:
                return "11:30 - 12:00";
            case 6:
                return "12:00 - 12:30";
            case 7:
                return "12:30 - 13:00";
            case 8:
                return "13:00 - 13:30";
            case 9:
                return "13:30 - 14:00";
            case 10:
                return "14:00 - 14:30";
            case 11:
                return "14:30 - 15:00";
            case 12:
                return "15:00 - 15:30";
            case 13:
                return "15:30 - 16:00";
            case 14:
                return "16:00 - 16:30";
            case 15:
                return "16:30 - 17:00";
            case 16:
                return "17:00 - 17:30";
            case 17:
                return "17:30 - 18:00";
            case 18:
                return "18:00 - 18:30";
            case 19:
                return "18:30 - 19:00";
            default:
                return "Closed";
        }

    }

    public static void updateToken(Context mContext, String token) {

        if (Common.currentBarber != null) {

            Token mToken = new Token();
            mToken.setToken(token);
            mToken.setTokenType(TOKEN_TYPE.BARBER);
            mToken.setUser(Common.currentBarber.getUsername());

            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_TOKENS_REFERANCE)
                    .child(Common.currentBarber.getBarberId())
                    .setValue(mToken)
                    .addOnFailureListener(e -> {
                        Log.e("GET_TOKEN_ERROR", e.getMessage());
                        Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnCompleteListener(task -> { });


        } else {

            Paper.init(mContext);
            String user = Paper.book().read(Common.KEY_LOGGED);
            if (user != null) {

                if (!TextUtils.isEmpty(user)) {

                    Token mToken = new Token();
                    mToken.setToken(token);
                    mToken.setUser(user);
                    mToken.setTokenType(TOKEN_TYPE.CLIENT);

                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_TOKENS_REFERANCE)
                            .child(Common.currentBarber.getBarberId())
                            .setValue(mToken)
                            .addOnFailureListener(e -> {
                                Log.e("GET_TOKEN_ERROR", e.getMessage());
                                Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnCompleteListener(task -> { });

                }
            }

        }

    }


    public static void showNotification(Context mContext, int id, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "ocean_for_it_eat_it";
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Eat It", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Eat It");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(R.mipmap.ic_launcher_round);
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static String formatName(String name) {

        return name.length() > 13 ? new StringBuilder(name.substring(0, 10))
                .append(" ...").toString() : name;
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {

        String result = null;
        if (fileUri.getScheme().equals("content")) {

            Cursor cursor = contentResolver.query(fileUri, null, null, null, null);

            try {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                cursor.close();
            }
        }

        if (result == null) {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }

        return result;
    }

    public static void setFragment(Fragment fragment, int id, FragmentManager fragmentManager) {

        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(id, fragment)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit();

    }

    public static void showSnackBar(Context context, @NonNull int layout, View view, @NonNull String message) {

        Snackbar mSnackBar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);

        View snackView = LayoutInflater.from(context)
                .inflate(layout, null);

        mSnackBar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) mSnackBar.getView();
        snackbarLayout.setPadding(0,0,0,0);

        FrameLayout.LayoutParams mFrameParams = (FrameLayout.LayoutParams) mSnackBar.getView().getLayoutParams();
        mFrameParams.gravity = Gravity.BOTTOM;
        mSnackBar.getView().setLayoutParams(mFrameParams);

        TextView tvMessage = snackView.findViewById(R.id.tv_message);
        tvMessage.setText(message);

        snackbarLayout.addView(snackView,0);
        mSnackBar.show();
    }

    public static void showSnackBar(Context context, @NonNull int layout, View view, @NonNull String message, int duration, View.OnClickListener listener) {

        Snackbar mSnackBar = Snackbar.make(view, "", duration);

        View snackView = LayoutInflater.from(context)
                .inflate(layout, null);

        mSnackBar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) mSnackBar.getView();
        snackbarLayout.setPadding(0,0,0,0);

        FrameLayout.LayoutParams mFrameParams = (FrameLayout.LayoutParams) mSnackBar.getView().getLayoutParams();
        mFrameParams.gravity = Gravity.BOTTOM;
        mSnackBar.getView().setLayoutParams(mFrameParams);

        TextView tvMessage = snackView.findViewById(R.id.tv_message);
        TextView retry = snackView.findViewById(R.id.tv_click_retry);
        tvMessage.setText(message);
        retry.setOnClickListener(listener);


        snackbarLayout.addView(snackView,0);
        mSnackBar.show();

    }

    public static void showProgressSnackBar(Context mContext, View view, String message) {

        Snackbar mSnackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        View mSnackBarView = mSnackBar.getView();
        mSnackBarView.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        FrameLayout.LayoutParams mFrameParams = (FrameLayout.LayoutParams) mSnackBarView.getLayoutParams();
        mFrameParams.gravity = Gravity.BOTTOM;
        mSnackBarView.setLayoutParams(mFrameParams);
        mSnackBar.show();
    }

    public static void showSnackBar(Context mContext, View view, String message) {

        Snackbar mSnackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View mSnackBarView = mSnackBar.getView();
        mSnackBarView.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        FrameLayout.LayoutParams mFrameParams = (FrameLayout.LayoutParams) mSnackBarView.getLayoutParams();
        mFrameParams.gravity = Gravity.BOTTOM;
        mSnackBarView.setLayoutParams(mFrameParams);
        mSnackBar.show();
    }

    public static void dismissSnackBar(Context mContext, View view, String message) {

        Snackbar mSnackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View mSnackBarView = mSnackBar.getView();
        mSnackBarView.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        FrameLayout.LayoutParams mFrameParams = (FrameLayout.LayoutParams) mSnackBarView.getLayoutParams();
        mFrameParams.gravity = Gravity.BOTTOM;
        mSnackBarView.setLayoutParams(mFrameParams);
        mSnackBar.show();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void customDialog(AlertDialog dialog){

        // custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    public static Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    public static File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Barber Booking Staff");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir())
                return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" +
                timeStamp + "_" + new Random().nextInt() + ".jpg");

        return mediaFile;
    }

    public enum TOKEN_TYPE {

        CLIENT,
        BARBER,
        MANAGER
    }

}
