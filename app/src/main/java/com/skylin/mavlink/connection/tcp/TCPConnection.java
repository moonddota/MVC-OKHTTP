package com.skylin.mavlink.connection.tcp;

import com.skylin.mavlink.connection.MavLinkConnection;
import com.skylin.mavlink.model.ConnectionParameter;
import com.skylin.mavlink.model.TcpConnectionParameter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by SJJ on 2017/3/29.
 */

public class TCPConnection extends MavLinkConnection {
    private static final int CONNECTION_TIMEOUT = 20 * 1000; // 20 secs in ms
    private TcpConnectionParameter parameter;
    private Socket socket;
    private BufferedOutputStream mavOut;
    private BufferedInputStream mavIn;

    public TCPConnection(TcpConnectionParameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter is null");
        }
        this.parameter = parameter;
    }

    @Override
    protected void openConnection() throws IOException {
        InetAddress serverAddr = InetAddress.getByName(parameter.getIp());
        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddr, parameter.getPort()), CONNECTION_TIMEOUT);
        mavOut = new BufferedOutputStream((socket.getOutputStream()));
        mavIn = new BufferedInputStream(socket.getInputStream());
    }

    @Override
    protected int readDataBlock(byte[] buffer) throws IOException, NullPointerException {
        return mavIn.read(buffer);
    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException, NullPointerException {
        mavOut.write(buffer);
        mavOut.flush();
    }

    @Override
    protected void closeConnection() throws IOException {
        if (socket != null)
            socket.close();
        socket = null;
        mavIn = null;
        mavOut = null;
    }

    @Override
    public int getConnectionType() {
        return ConnectionParameter.tcp;
    }
}
