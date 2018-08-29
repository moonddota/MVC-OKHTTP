package com.skylin.uav.drawforterrain.select.gps_hand;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.skylin.uav.R;

public class TbaseAdapter extends ArrayAdapter<Device> {
    private int mResourceId;
    private Context mContext;

    public TbaseAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.mResourceId = resource;
        this.mContext = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Device device = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(mResourceId, null);
        TextView nameText = (TextView) view.findViewById(R.id.device_name);
        TextView adress = (TextView) view.findViewById(R.id.device_adress);

        nameText.setText(device.getName());
        adress.setText(device.getAdress());


        return view;
    }

}