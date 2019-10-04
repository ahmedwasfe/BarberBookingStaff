package com.ahmet.barberbookingstaff.Adapter;

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

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Common.SheetDailogLogin;
import com.ahmet.barberbookingstaff.HomeStaffActivity;
import com.ahmet.barberbookingstaff.Interface.IDailogClickListener;
import com.ahmet.barberbookingstaff.Interface.IGetBarberListener;
import com.ahmet.barberbookingstaff.Interface.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.Interface.IUserLoginRemebmberListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.Model.Salon;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

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

        holder.mTxtSalonName.setText(mListSalon.get(position).getName());
        holder.mTxtSalonAddress.setText(mListSalon.get(position).getAddress());

        holder.setItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {

                Common.currentSalon = mListSalon.get(position);
                showSheetDailogLogin();
            }
        });

    }

    private void showSheetDailogLogin() {

        SheetDailogLogin.getInstance()
                .showLoginDailog(mContext,
                        "STAFF LOGIN",
                        "LOG IN",
                        "CANCEL",
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
                .collection("AllSalon")
                .document(Common.currentSalon.getSalonID())
                .collection("Barber")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){
                            if (task.getResult().size() > 0){

                                sheetDialog.dismiss();
                                mDialogLoading.dismiss();

                                iUserLoginRemebmberListener.onUserLoginSuccess(username);

                                Barber barber = new Barber();
                                for (DocumentSnapshot barberSnapshot : task.getResult()){
                                    barber = barberSnapshot.toObject(Barber.class);
                                    barber.setBarberID(barberSnapshot.getId());
                                }

                                iGetBarberListener.onGetBarberSuccess(barber);

                                // We will navigate Staff Home and clear Previous activity
                                Intent staffHomeIntent = new Intent(mContext, HomeStaffActivity.class);
                                staffHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                staffHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(staffHomeIntent);

                            } else {
                                mDialogLoading.dismiss();
                                Toast.makeText(mContext, "Wrong username and password or wrong salon", Toast.LENGTH_SHORT).show();

                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                mDialogLoading.dismiss();
            }
        });

    }

    @Override
    public void onClickPositiveButton(BottomSheetDialog sheetDialog, String name, String username, String password, String barberType) {

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
