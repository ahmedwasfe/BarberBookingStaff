package com.ahmet.barberbookingstaff.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.model.Salon;
import com.google.protobuf.Internal;

import java.util.List;

public class SearchSalonAdapter extends ArrayAdapter {

    private Context context;
    private List<Salon> listSalon;
    private LayoutInflater inflater;

    public SearchSalonAdapter(Context context, List<Salon> listSalon) {
        super(context, 0);
        this.context = context;
        this.listSalon = listSalon;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listSalon.size();
    }

    @Override
    public Object getItem(int position) {
        return listSalon.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(listSalon.get(position).getSalonName());

        return convertView;
    }
}
