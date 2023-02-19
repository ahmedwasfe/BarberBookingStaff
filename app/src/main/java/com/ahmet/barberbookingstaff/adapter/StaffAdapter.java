package com.ahmet.barberbookingstaff.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.callback.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
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

        Picasso.get()
                .load(mListStaff.get(position).getImage())
                .placeholder(R.drawable.hairdresser)
                .into(holder.mImgBarber);


        holder.setiRecyclerItemSelectedListener((view, position1) -> {

            FirebaseFirestore.getInstance().collection(Common.KEY_AllSALON_REFERANCE)
                    .document(Common.currentSalon.getSalonId())
                    .collection(Common.KEY_BARBER_REFERANCE)
                    .document(Common.currentBarber.getBarberId())
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                Common.currentBarber = mListStaff.get(position1);
                                Common.currentBarber.setBarberId(snapshot.getId());

                            }
                        }
                    });


        });
    }

    @Override
    public int getItemCount() {
        return mListStaff.size();
    }

    public Barber getItemAtPosition(int position) {
        return mListStaff.get(position);
    }

    static class StaffHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTxtStaffname, mTxtStaffUsername, mTxtStaffType;
        CircleImageView mImgBarber;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public StaffHolder(@NonNull View itemView) {
            super(itemView);

            mTxtStaffname = itemView.findViewById(R.id.txt_staff_name);
            mTxtStaffUsername = itemView.findViewById(R.id.txt_staff_username);
            mTxtStaffType = itemView.findViewById(R.id.txt_staff_type);
            mImgBarber = itemView.findViewById(R.id.img_barber);


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
