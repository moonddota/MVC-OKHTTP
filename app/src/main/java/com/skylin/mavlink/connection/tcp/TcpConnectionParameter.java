package com.skylin.mavlink.connection.tcp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SJJ on 2017/m3/29.
 */

public class TcpConnectionParameter extends ConnectionParameter implements Parcelable {
    private String ip;
    private int port;

    public TcpConnectionParameter(String ip, int port) {
        super(tcp);
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TcpConnectionParameter that = (TcpConnectionParameter) o;

        if (port != that.port) return false;
        return ip != null ? ip.equals(that.ip) : that.ip == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "TcpConnectionParameter{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    private TcpConnectionParameter(Parcel in) {
        super(tcp);
        ip = in.readString();
        port = in.readInt();
    }

    public static final Creator<TcpConnectionParameter> CREATOR = new Creator<TcpConnectionParameter>() {
        @Override
        public TcpConnectionParameter createFromParcel(Parcel in) {
            return new TcpConnectionParameter(in);
        }

        @Override
        public TcpConnectionParameter[] newArray(int size) {
            return new TcpConnectionParameter[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeInt(port);
    }
}
