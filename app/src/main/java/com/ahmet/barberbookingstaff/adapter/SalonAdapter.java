package com.ahmet.barberbookingstaff.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.common.SheetDialogLogin;
import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.callback.IDailogClickListener;
import com.ahmet.barberbookingstaff.callback.IGetBarberListener;
import com.ahmet.barberbookingstaff.callback.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.callback.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SalonAdapter extends RecyclerView.Adapter<SalonAdapter.SalonHolder> implements IDailogClickListener {

    private Context mContext;
    private List<Salon> mListSalon;
    private LayoutInflater inflater;

    private IUserLoginRemebmberListener iUserLoginRemebmberListener;
    private IGetBarberListener iGetBarberListener;

    public SalonAdapter(Context mContext, List<Salon> mListSalon,
                        IUserLoginRemebmberListener iUserLoginRemebmberListener, IGetBarberListener iGetBarberListener) {
        this.mContext = mContext;
        this.mListSalon = mListSalon;
        this.iUserLoginRemebmberListener = iUserLoginRemebmberListener;
        this.iGetBarberListener = iGetBarberListener;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public SalonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_salon, parent, false);
        return new SalonHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull SalonHolder holder, int position) {

       // holder.mTxtSalonName.setText(Common.formatName(mListSalon.get(position).getName()));
        holder.mTxtSalonName.setText((mListSalon.get(position).getSalonName()));
        holder.mTxtSalonAddress.setText(mListSalon.get(position).getCity());

        holder.setItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {

                Common.currentSalon = mListSalon.get(position);
                showSheetDailogLogin(mListSalon.get(position).getEmail());
            }
        });

    }

    private void showSheetDailogLogin(String email) {

        SheetDialogLogin.getInstance()
                .showLoginDailog(mContext,
                        email,
                        mContext.getString(R.string.staff_login),
                        mContext.getString(R.string.log_in),
                        mContext.getString(R.string.cancel),
                        this);
    }

    @Override
    public int getItemCount() {
        return mListSalon.size();
    }

    @Override
    public void onClickPositiveButton(BottomSheetDialog sheetDialog, String username, String password) {

        AlertDialog mDialogLoading = new SpotsDialog.Builder()
                .setContext(mContext)
                .setCancelable(false)
                .build();
        mDialogLoading.show();

        // /AllSalon/Gaza/Branch/AFXjgtlJwztf7cLFumNT/Barber/utQmhc07WVjaZdr9tbRB
        FirebaseFirestore.getInstance()
                .collection(Common.KEY_AllSALON_REFERANCE)
                .document(Common.currentSalon.getEmail())
                .collection(Common.KEY_BARBER_REFERANCE)
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        if (task.getResult().size() > 0){

                            sheetDialog.dismiss();
                            mDialogLoading.dismiss();

                            iUserLoginRemebmberListener.onUserLoginSuccess(mContext, username);
                            Paper.init(mContext);
                            Paper.book().write(Common.KEY_LOGGED, username);


                            Barber barber = new Barber();
                            for (DocumentSnapshot barberSnapshot : task.getResult()){
                                barber = barberSnapshot.toObject(Barber.class);
                                barber.setBarberId(barberSnapshot.getId());
                            }

                            iGetBarberListener.onGetBarberSuccess(mContext, barber);

                            // We will navigate Staff Home and clear Previous activity
                            Intent staffHomeIntent = new Intent(mContext, HomeStaffActivity.class);
                            staffHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            staffHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(staffHomeIntent);

                        } else {
                            mDialogLoading.dismiss();
                            Toast.makeText(mContext, mContext.getString(R.string.wrong_username_or_password), Toast.LENGTH_SHORT).show();

                        }
                    }

                }).addOnFailureListener(e -> {
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    mDialogLoading.dismiss();
                });

    }

    @Override
    public void onClickPositiveButton(BottomSheetDialog sheetDialog, String name,
                                      String username, String password, String barberType) {

    }

    @Override
    public void onClickNegativeButton(BottomSheetDialog sheetDialog) {

        sheetDialog.dismiss();
    }


    static class SalonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTxtSalonName, mTxtSalonAddress;
        CardView mCardSalon;

        IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public SalonHolder(@NonNull View itemView) {
            super(itemView);

            mTxtSalonName = itemView.findViewById(R.id.txt_salon_name);
            mTxtSalonAddress = itemView.findViewById(R.id.txt_salon_address);
            mCardSalon = itemView.findViewById(R.id.card_salon);

            itemView.setOnClickListener(this);
        }

        public void setItemSelectedListener(IRecyclerItemSelectedListener mIRecyclerItemSelectedListener) {
            this.mIRecyclerItemSelectedListener = mIRecyclerItemSelectedListener;
        }



        @Override
        public void onClick(View view) {
            mIRecyclerItemSelectedListener.onItemSelected(view, getAdapterPosition());
        }

    }
}
