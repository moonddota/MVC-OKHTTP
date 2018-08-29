package com.skylin.uav.drawforterrain.select.gps_hand;

import android.bluetooth.BluetoothDevice;

public class Device {
    private String name;
    private String adress;

    public Device(String name, String adress) {
        this.name = name;
        this.adress = adress;
    }

    public Device(BluetoothDevice device) {
        this.name = device.getName();
        this.adress = device.getAddress();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }
}
