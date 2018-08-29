package com.skylin.mavlink.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SJJ on 2017/4/5.
 */

public class UsbConnectionParameter extends ConnectionParameter implements Parcelable {
    private int baudRate;

    public UsbConnectionParameter(int baudRate) {
        super(usb);
        this.baudRate = baudRate;
    }

    private UsbConnectionParameter(Parcel in) {
        super(usb);
        baudRate = in.readInt();
    }

    public int getBaudRate() {
        return baudRate;
    }

    public static final Creator<UsbConnectionParameter> CREATOR = new Creator<UsbConnectionParameter>() {
        @Override
        public UsbConnectionParameter createFromParcel(Parcel in) {
            return new UsbConnectionParameter(in);
        }

        @Override
        public UsbConnectionParameter[] newArray(int size) {
            return new UsbConnectionParameter[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsbConnectionParameter)) return false;
        if (!super.equals(o)) return false;

        UsbConnectionParameter that = (UsbConnectionParameter) o;

        return baudRate == that.baudRate;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + baudRate;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(baudRate);
    }
}
