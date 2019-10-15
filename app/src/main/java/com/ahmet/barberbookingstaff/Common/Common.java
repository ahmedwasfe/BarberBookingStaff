package com.ahmet.barberbookingstaff.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.BookingInformation;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.Model.Token;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.file.OpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.paperdb.Paper;

public class Common {


    public static final String KEY_LOGGED = "LOGGED";
    public static final String KEY_LOGGED_EMAIL = "LOGGED_EMAIL";
    public static String IS_LOGIN = "IsLogin";
    public static final String KEY_CITY = "CITY";
    public static final String KEY_SALON = "SALON";
    public static final String KEY_BARBER = "BARBER";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";

    public static final String KEY_RATING_CITY = "RATING_CITY";
    public static final String KEY_RATING_SALON_ID = "RATING_SALON_ID";
    public static final String KEY_RATING_SALON_NAME = "RATING_SALON_NAME";
    public static final String KEY_RATING_BARBER_ID = "RATING_BARBER_ID";

    public static final String SERVICES_ADDED = "SERVICES_ADDED";
    public static final String SHOPPING_ITEMS = "SHOPPING_ITEMS";
    public static final String MONEY_SIGN = "$";
    public static final String IMAGE_DOWNLIADABLE_URL = "DOWNLIADABLE_URL";


    public static String cityName = "";
    public static Salon currentSalon;
    public static Barber currentBarber;
    public static BookingInformation currentBooking;

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


    public static String convertTimeSoltToString(int solt) {

        switch (solt){

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

        /* * Fires we need check if user still login
           * Because, we need store token be longing user
           * So, we need use store data
           *
         */

        Paper.init(mContext);
        String user = Paper.book().read(Common.KEY_LOGGED);

        if (user != null){
            if (!TextUtils.isEmpty(user)){

                Token mToken = new Token();
                mToken.setToken(token);
                // Because this code run from Barber Staff app
                mToken.setTokenType(TOKEN_TYPE.BARBER);
                mToken.setUser(user);

                // Submit to firebase firestore
                FirebaseFirestore.getInstance()
                        .collection("Tokens")
                        .document(user)
                        .set(mToken)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            }
        }
    }

//    public static void testService(Context context){
//        Log.d("ShowNotifiaction", "Show Notifiaction");
//    }

    public static void showNotification(Context mContext, int notificationId, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;

        if (intent != null) {

            pendingIntent = PendingIntent.getActivity(mContext,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

            String NOTIFICATION_CHANNEL = "sajahmet_barber_booking_staff_app_channel_01";
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){

                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL,
                        "SAJAHMET Barber Booking Staff App", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription("Barber Booking Staff App");
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);

                notificationManager.createNotificationChannel(notificationChannel);
            }
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL);
            builder.setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(false)
                    .setSound(sound)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));

            if (pendingIntent != null)
                builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();
            notificationManager.notify(notificationId, notification);
    }

    public static String formatShoppingName(String name) {

        return name.length() > 13 ? new StringBuilder(name.substring(0, 10))
                .append(" ...").toString() : name;
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {

        String result = null;
        if (fileUri.getScheme().equals("content")){

            Cursor cursor = contentResolver.query(fileUri, null, null, null, null);

            try{
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                cursor.close();
            }
        }

        if (result == null){
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }

        return result;
    }

    public static void setFragment(Fragment fragment, int id, FragmentManager fragmentManager){

        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(id, fragment)
                .commit();

    }

    public enum TOKEN_TYPE {

        CLIENT,
        BARBER,
        MANAGER
    }

}
