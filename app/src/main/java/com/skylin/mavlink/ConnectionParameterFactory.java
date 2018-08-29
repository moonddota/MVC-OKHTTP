package com.skylin.mavlink;

import com.skylin.mavlink.model.ConnectionParameter;
import com.skylin.mavlink.model.SerialConnectionParameter;
import com.skylin.mavlink.model.TcpConnectionParameter;
import com.skylin.mavlink.model.UsbConnectionParameter;

/**
 * Created by SJJ on 2017/3/30.
 */

public class ConnectionParameterFactory {
    private final static String TCP_ip = "192.168.2.251";//192.168.0.99
    private final static int TCP_port = 6789;
    private final static int baudRate = 57600;

    public static TcpConnectionParameter getTcpConnectionParameter() {
        return new TcpConnectionParameter(TCP_ip, TCP_port);
    }

    public static UsbConnectionParameter getUsbConnectionParameter() {
        return new UsbConnectionParameter(baudRate);
    }

    public static ConnectionParameter getConnectionParameter(int type) {
        switch (type) {
            case ConnectionParameter.tcp:
                return getTcpConnectionParameter();
            case ConnectionParameter.usb:
                return getUsbConnectionParameter();
            case ConnectionParameter.serial:
                return getSerialConnectionParameter();
        }
        throw new IllegalArgumentException("Unsupported Connection Parameter");
    }

    public static SerialConnectionParameter getSerialConnectionParameter() {
        return new SerialConnectionParameter(baudRate);
    }
}
