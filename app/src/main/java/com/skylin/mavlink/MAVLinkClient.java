package com.skylin.mavlink;

import android.content.Context;

import com.skylin.mavlink.connection.MavLinkConnection;
import com.skylin.mavlink.connection.serial.SerialConnection;
import com.skylin.mavlink.connection.tcp.TCPConnection;
import com.skylin.mavlink.connection.usb.UsbConnection;
import com.skylin.mavlink.model.ConnectionParameter;
import com.skylin.mavlink.model.SerialConnectionParameter;
import com.skylin.mavlink.model.TcpConnectionParameter;
import com.skylin.mavlink.model.UsbConnectionParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SJJ on 2017/3/29.
 */

public class MAVLinkClient {
    private Map<ConnectionParameter, MavLinkUavClient> clientMap = new HashMap<>();

    public synchronized MavLinkUavClient getUavClient(ConnectionParameter parameter, Context context) {
        MavLinkUavClient uavClient = clientMap.get(parameter);
        if (uavClient != null) {
            return uavClient;
        }
        MavLinkConnection connection;
        switch (parameter.getConnectionType()) {
            case ConnectionParameter.tcp:
                connection = new TCPConnection((TcpConnectionParameter) parameter);
                break;
            case ConnectionParameter.usb:
                connection = new UsbConnection(context,(UsbConnectionParameter) parameter);
                break;
            case ConnectionParameter.serial:
                connection = new SerialConnection((SerialConnectionParameter) parameter);
                break;
            default:
                throw new RuntimeException("Unsupported connection");
        }
        MavLinkUavClient uavClient1 = new MavLinkUavClient(connection);
        clientMap.put(parameter, uavClient1);
        return uavClient1;
    }

}
