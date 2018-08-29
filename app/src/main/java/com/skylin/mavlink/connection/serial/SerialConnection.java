package com.skylin.mavlink.connection.serial;

import com.skylin.mavlink.connection.MavLinkConnection;
import com.skylin.mavlink.model.ConnectionParameter;
import com.skylin.mavlink.model.SerialConnectionParameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialUtils;

public class SerialConnection extends MavLinkConnection {
    private boolean waitBind;
    private final SerialConnectionParameter parameter;
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    // ----------------------------------------------------
    public SerialConnection(SerialConnectionParameter parameter) {
        this.parameter = parameter;
    }
    public boolean enable() {
        return true;
    }

    @Override
    public void openConnection() throws IOException {
        mSerialPort = new SerialPort(new File("/dev/ttyHSL0"), parameter.getBaudRate(), 0, 8, 1, 'N');
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
    }

    @Override
    public int readDataBlock(byte[] buffer) throws IOException {
        return mInputStream.read(buffer);
    }

    @Override
    public void sendBuffer(byte[] buffer) throws IOException {
        mOutputStream.write(buffer);
    }

    @Override
    public void closeConnection() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        mInputStream = null;
        mOutputStream = null;
    }

    @Override
    public int getConnectionType() {
        return ConnectionParameter.serial;
    }
}