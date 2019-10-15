package com.ahmet.barberbookingstaff.Adapter;

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
import com.ahmet.barberbookingstaff.SubActivity.DoneServicsesActivity;
import com.ahmet.barberbookingstaff.Interface.IRecyclerItemSelectedListener;
import com.ahmet.barberbookingstaff.Model.BookingInformation;
import com.ahmet.barberbookingstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSoltHolder> {

    private Context mContext;

    private List<BookingInformation> mListTimeSlot;
    private List<CardView> mListCardTimeSolt;

    private LayoutInflater inflater;

    public TimeSlotAdapter(Context mContext) {

        this.mContext = mContext;

        this.mListTimeSlot = new ArrayList<>();
        mListCardTimeSolt = new ArrayList<>();

        inflater = LayoutInflater.from(mContext);

    }

    public TimeSlotAdapter(Context mContext, List<BookingInformation> mListTimeSlot) {

        this.mContext = mContext;

        this.mListTimeSlot = mListTimeSlot;
        mListCardTimeSolt = new ArrayList<>();

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public TimeSoltHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.raw_time_solt, parent, false);

        return new TimeSoltHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSoltHolder holder, int position) {

        holder.mTxtTimeSolt.setText(new StringBuilder(Common.convertTimeSoltToString(position)).toString()  );


        if (mListTimeSlot.size() == 0){  // If all position available , just show list

            holder.mCardTimeSolt.setCardBackgroundColor(
                    mContext.getResources().getColor(R.color.colorWhite));

            holder.mTxtTimeSoltDescription.setText("Available");
            holder.mTxtTimeSoltDescription.setTextColor(
                    mContext.getResources().getColor(R.color.colorBlack));

            holder.mTxtTimeSolt.setTextColor(
                    mContext.getResources().getColor(R.color.colorBlack));
           // holder.mCardTimeSolt.setEnabled(true);

            holder.setmIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelected(View view, int position) {
                    // Fix crach if we not add this function
                }
            });


        }else {  // If have position full (booked)

            for (BookingInformation slotValue : mListTimeSlot){

                // Loop all time solt from server and set a differnt color
                int slot = Integer.parseInt(slotValue.getTimeSlot().toString());

                if (slot == position) {  // If time slot == position

                    if (!slotValue.isDone()) {

                        // I Will set tag for all time slot in full
                        // So base on tag , i can set all remain card background without change full time slot
                        holder.mCardTimeSolt.setTag(Common.DISABLE_TAG);

                        holder.mCardTimeSolt.setCardBackgroundColor(
                                mContext.getResources().getColor(R.color.colorPrimary));

                        holder.mTxtTimeSoltDescription.setText("Full");
                        holder.mTxtTimeSoltDescription.setTextColor(
                                mContext.getResources().getColor(R.color.colorWhite));

                        holder.mTxtTimeSolt.setTextColor(
                                mContext.getResources().getColor(R.color.colorWhite));
                        // holder.mCardTimeSolt.setEnabled(false);

                        holder.setmIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {
                                // Only add for gray time slot
                                // Here we will get Booking Information and store in Common.currentBookingInformation
                                // After that start Done Service Actvivty

                                FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.currentSalon.getSalonID())
                                        .collection("Barber")
                                        .document(Common.currentBarber.getBarberID())
                                        .collection(Common.mSimpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(slotValue.getTimeSlot().toString())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                if (task.isSuccessful()) {

                                                    if (task.getResult().exists()) {

                                                        Common.currentBooking = task.getResult().toObject(BookingInformation.class);
                                                        Common.currentBooking.setBookingID(task.getResult().getId());
                                                        mContext.startActivity(new Intent(mContext, DoneServicsesActivity.class));
                                                    }
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } else {

                        // If Service done
                        holder.mCardTimeSolt.setTag(Common.DISABLE_TAG);

                        holder.mCardTimeSolt.setCardBackgroundColor(
                                mContext.getResources().getColor(R.color.colorRed));

                        holder.mTxtTimeSoltDescription.setText("Done");
                        holder.mTxtTimeSoltDescription.setTextColor(
                                mContext.getResources().getColor(R.color.colorWhite));

                        holder.mTxtTimeSolt.setTextColor(
                                mContext.getResources().getColor(R.color.colorWhite));

                        holder.setmIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {
                                // Fix here to crash
                            }
                        });
                    }

                } else {

                    if (holder.getmIRecyclerItemSelectedListener() == null){

                        /* We only add event for view holder whitch is not implementclick
                         * Because if we donوt put this condition
                         * All time slot with value higher current time slot will be override event
                        */
                        holder.setmIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {
                                // Fix crach
                            }
                        });
                    }
                }
            }
        }

        // Add only available time slot card to list
        // Add all card to list (20 card because i have 20 time slot)
        // No add card aleredy in cardViewList
        if (!mListCardTimeSolt.contains(holder.mCardTimeSolt))
            mListCardTimeSolt.add(holder.mCardTimeSolt);

        // check if card time slot is available
//        if (!mListTimeSlot.contains(position)) {
//            holder.setmIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
//                @Override
//                public void onItemSelected(View view, int position1) {
//
//                    // Loop all cards in card list
//                    for (CardView cardView : mListCardTimeSolt) {
//                        // Only available card time slot be change
//                        if (cardView.getTag() == null)
//                            cardView.setCardBackgroundColor(
//                                    mContext.getResources().getColor(R.color.colorWhite));
//                    }
//
//                    // Our selected card will be change color
//                    holder.mCardTimeSolt.setCardBackgroundColor(
//                            mContext.getResources().getColor(R.color.colorAccent));
//                    holder.mTxtTimeSolt.setTextColor(
//                            mContext.getResources().getColor(R.color.colorWhite));
//                    holder.mTxtTimeSoltDescription.setTextColor(
//                            mContext.getResources().getColor(R.color.colorWhite));
//
//                }
//            });
//        }

    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }


    static class TimeSoltHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTxtTimeSolt, mTxtTimeSoltDescription;
        CardView mCardTimeSolt;

        IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public TimeSoltHolder(@NonNull View itemView) {
            super(itemView);

            mTxtTimeSolt = itemView.findViewById(R.id.txt_time_solt);
            mTxtTimeSoltDescription = itemView.findViewById(R.id.txt_time_solt_description);
            mCardTimeSolt = itemView.findViewById(R.id.card_time_solt);

            itemView.setOnClickListener(this);
        }

        public IRecyclerItemSelectedListener getmIRecyclerItemSelectedListener() {
            return mIRecyclerItemSelectedListener;
        }

        public void setmIRecyclerItemSelectedListener(IRecyclerItemSelectedListener mIRecyclerItemSelectedListener) {
            this.mIRecyclerItemSelectedListener = mIRecyclerItemSelectedListener;
        }

        @Override
        public void onClick(View view) {
            mIRecyclerItemSelectedListener.onItemSelected(view, getAdapterPosition());
        }
    }
}
