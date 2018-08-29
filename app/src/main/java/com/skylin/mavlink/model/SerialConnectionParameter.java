package com.skylin.mavlink.model;

/**
 * Created by sjj on 2017/7/10.
 */

public class SerialConnectionParameter extends ConnectionParameter {
    private final int baudRate;

    public SerialConnectionParameter(int baudRate) {
        super(serial);
        this.baudRate = baudRate;
    }

    public int getBaudRate() {
        return baudRate;
    }
}
