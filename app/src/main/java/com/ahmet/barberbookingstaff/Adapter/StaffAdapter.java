package com.ahmet.barberbookingstaff.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Interface.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.Model.Barber;
import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.SubActivity.UpdateStaffActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffHolder> {

    private Context mContext;
    private List<Barber> mListStaff;
    private LayoutInflater inflater;

    public StaffAdapter(Context mContext, List<Barber> mListStaff) {
        this.mContext = mContext;
        this.mListStaff = mListStaff;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public StaffHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_staff, parent, false);

        return new StaffHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffHolder holder, int position) {

        AlertDialog mDialog = new SpotsDialog.Builder()
                .setContext(mContext)
                .build();

        holder.mTxtStaffname.setText(mListStaff.get(position).getName());
        holder.mTxtStaffUsername.setText(mListStaff.get(position).getUsername());
        holder.mTxtStaffType.setText(mListStaff.get(position).getBarberType());


        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {

//                Intent intent = new Intent(mContext, UpdateStaffActivity.class);
//                intent.putExtra("barber", mListStaff.get(position));
//                mContext.startActivity(intent);

                FirebaseFirestore.getInstance().collection("AllSalon")
                        .document(Common.currentSalon.getSalonID())
                        .collection("Barber")
                        .document(Common.currentBarber.getBarberID())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot snapshot = task.getResult();
                                    if (snapshot.exists()) {
                                        Common.currentBarber = mListStaff.get(position);
                                        Common.currentBarber.setBarberID(snapshot.getId());
                                        mContext.startActivity(new Intent(mContext, UpdateStaffActivity.class));
                                    }
                                }
                            }
                        });


            }
        });
    }

    @Override
    public int getItemCount() {
        return mListStaff.size();
    }

    static class StaffHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTxtStaffname, mTxtStaffUsername, mTxtStaffType;
        ImageView mImgRemoveStaff, mImgUpdateStaff;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public StaffHolder(@NonNull View itemView) {
            super(itemView);

            mTxtStaffname = itemView.findViewById(R.id.txt_staff_name);
            mTxtStaffUsername = itemView.findViewById(R.id.txt_staff_username);
            mTxtStaffType = itemView.findViewById(R.id.txt_staff_type);
            mImgRemoveStaff = itemView.findViewById(R.id.img_remove_staff);
            mImgUpdateStaff = itemView.findViewById(R.id.img_update_staff);

            itemView.setOnClickListener(this);


        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}
