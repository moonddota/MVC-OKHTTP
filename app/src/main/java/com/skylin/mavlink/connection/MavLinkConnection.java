package com.skylin.mavlink.connection;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Parser;
import com.MAVLink.common.msg_heartbeat;
import com.blankj.utilcode.util.EmptyUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.digi.xbee.api.models.OperatingMode;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeTransmitOptions;
import com.digi.xbee.api.packet.XBeePacket;
import com.digi.xbee.api.packet.common.ATCommandPacket;
import com.digi.xbee.api.packet.common.ATCommandResponsePacket;
import com.digi.xbee.api.packet.common.ReceivePacket;
import com.digi.xbee.api.packet.common.TransmitPacket;
import com.digi.xbee.api.packet.common.TransmitStatusPacket;
import com.skylin.mavlink.ACKHandler;
import com.skylin.mavlink.exception.NoUavException;
import com.skylin.mavlink.handler.ConnectHandler;
import com.skylin.mavlink.utils.Pool;
import com.skylin.mavlink.utils.Timer;
import com.skylin.mavlink.xbee.XBeeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import sjj.alog.Log;

/**
 * Base for mavlink connection implementations.
 */
public abstract class MavLinkConnection {
    /*
     * MavLink connection states
     */
    public static final int MAVLINK_DISCONNECTED = 0;
    public static final int MAVLINK_CONNECTING = 1;
    public static final int MAVLINK_CONNECTED = 2;
    private volatile int mConnectionStatus = MAVLINK_DISCONNECTED;
    /**
     * Size of the buffer used to read messages from the mavlink connection.
     */
    private static final int READ_BUFFER_SIZE = 4096;

    /**
     * Maximum possible sequence number for a packet.
     */
    private static final int MAX_PACKET_SEQUENCE = 255;

    /**
     * Set of listeners subscribed to this mavlink connection. We're using a
     * ConcurrentSkipListSet because the object will be accessed from multiple
     * threads concurrently.
     */
    private MavLinkConnectionListener mavLinkConnectionListener;

    /**
     * Queue the set of packets to send via the mavlink connection. A thread
     * will be blocking on it until there's element(s) available to send.
     */
    private final LinkedBlockingQueue<ACKHandler> mPacketsToSend = new LinkedBlockingQueue<>();

    private List<ConnectHandler> connectHandlers = new CopyOnWriteArrayList<>();
    private List<ConnectHandler> connectHandlersSend = new ArrayList<>();
    private int msgSeqNumber = 1;
    public String currentMac = "";
    /**
     * Listen for incoming data on the mavlink connection.
     */
    private final Runnable mConnectingTask = new Runnable() {
        XBeeParser xBeeParser = new XBeeParser();

        @Override
        public void run() {
            Thread sendingThread = null;

            // Load the connection specific preferences

            try {
                // Open the connection
                openConnection();
//                mConnectionStatus.set(MAVLINK_CONNECTED);
//                reportConnect();

                // Launch the 'Sending', and 'Logging' threads
//                Set<String> xbeeMacs = App.getApp().getConfig().getXbeeMacs();
//                if (xbeeMacs.isEmpty() && App.getApp().getConfig().getXbeeMode() != OperatingMode.AT) {
//                    throw new NoUavException("未设置连接飞机");
//                }
                sendingThread = new Thread(mSendingTask, "MavLinkConnection-Sending Thread");
                sendingThread.start();
//                if (App.getApp().getConfig().getXbeeMode() != OperatingMode.AT)
//                    connectTimer.start(0);
                final Parser parser = new Parser();
                parser.stats.resetStats();
                xBeeParser.reset();
                final byte[] readBuffer = new byte[READ_BUFFER_SIZE];

                while (mConnectionStatus != MAVLINK_DISCONNECTED) {
                    int bufferSize = readDataBlock(readBuffer);
                    handleData(parser, bufferSize, readBuffer);
                }
            } catch (Exception e) {
                // Ignore errors while shutting down
                if (mConnectionStatus != MAVLINK_DISCONNECTED) {
                    reportComError(e);
                }
            } finally {
                if (sendingThread != null && sendingThread.isAlive()) {
                    sendingThread.interrupt();
                }
                disconnect();
            }
        }

        private void handleData(Parser parser, int bufferSize, byte[] buffer) {
            if (bufferSize < 1) {
                return;
            }

//            StringBuilder sb = new StringBuilder(bufferSize*3);
//            for (int i = 0; i < bufferSize; i++) {
//                sb.append(String.format("%02X ", buffer[i]));
//            }
//            Log.e(sb);


//            if (App.getApp().getConfig().getXbeeMode() == OperatingMode.API) {
//                for (int i = 0; i < bufferSize; i++) {
//                    XBeePacket parse = xBeeParser.parse(buffer[i]);
//                    if (parse instanceof ReceivePacket) {
//                        String s = ((ReceivePacket) parse).get64bitSourceAddress().toString();
//                        if (mConnectionStatus == MAVLINK_CONNECTING) {
//                            Set<String> xbeeMacs = App.getApp().getConfig().getXbeeMacs();
//                            if (((ReceivePacket) parse).isBroadcast() || !xbeeMacs.contains(s)) {
//                                continue;
//                            }
//                            mConnectionStatus = MAVLINK_CONNECTED;
//                            currentMac = s;
//                            connectTimer.stop();
//                            reportConnectionStateChange(mConnectionStatus);
//                        } else if (!currentMac.equals(s)) {
//                            continue;
//                        }
//                        byte[] rfData = ((ReceivePacket) parse).getRFData();
//                        for (byte b : rfData) {
//                            MAVLinkPacket receivedPacket = parser.mavlink_parse_char(b & 0x00ff);
//                            if (receivedPacket != null) {
//                                reportReceivedPacket(receivedPacket);
//                            }
//                        }
//                    } else if (parse instanceof ATCommandResponsePacket) {
//                        if (mConnectionStatus == MAVLINK_CONNECTING) {
//                            for (ConnectHandler handler : connectHandlers) {
//                                handler.handle((ATCommandResponsePacket) parse);
//                            }
//                        }
//                    } else if (parse instanceof TransmitStatusPacket && (mConnectionStatus == MAVLINK_CONNECTING)) {
//                        for (ConnectHandler handler : connectHandlers) {
//                            handler.handle((TransmitStatusPacket) parse);
//                        }
//                    }
//                }
//            } else {
                for (int i = 0; i < bufferSize; i++) {
                    if (mConnectionStatus == MAVLINK_CONNECTING) {
                        mConnectionStatus = MAVLINK_CONNECTED;
                        reportConnectionStateChange(mConnectionStatus);
                    }
                    MAVLinkPacket receivedPacket = parser.mavlink_parse_char(buffer[i] & 0x00ff);
                    if (receivedPacket != null) {
                        reportReceivedPacket(receivedPacket);
                    }
                }
//            }
        }
    };
    private final Timer connectTimer = new Timer(new Runnable() {
        @Override
        public void run() {
            try {
                if (mConnectionStatus == MAVLINK_CONNECTED || mConnectionStatus == MAVLINK_DISCONNECTED) {
                    return;
                }
                for (ConnectHandler handler : connectHandlersSend) {
                    if (handler.isTimeOut()) {
                        sendBuffer(handler.generateByteArray(msgSeqNumber++));
                        if (msgSeqNumber == 256) {
                            msgSeqNumber = 1;
                        }
                    }
                }
            } catch (Exception ignored) {
                disconnect();
            }
        }
    }, 2000);
    /**
     * Blocks until there's packet(s) to send, then dispatch them.
     */
    private final Runnable mSendingTask = new Runnable() {
        @Override
        public void run() {
            while (mConnectionStatus == MAVLINK_CONNECTING) ;

            try {
                while (mConnectionStatus == MAVLINK_CONNECTED) {
                    ACKHandler take = mPacketsToSend.take();
                    if (!take.isActive())continue;
                    MAVLinkMessage message = take.getMessage();
                    MAVLinkPacket pack = message.pack();
                    pack.seq = msgSeqNumber++;
                    byte[] buffer = pack.encodePacket();
                    try {

//                        if (App.getApp().getConfig().getXbeeMode() == OperatingMode.API) {
//                            if (!EmptyUtils.isEmpty(currentMac)) {
//                                byte[] bytes = new TransmitPacket(pack.seq, new XBee64BitAddress(currentMac), XBee16BitAddress.UNKNOWN_ADDRESS, 0, XBeeTransmitOptions.NONE, buffer).generateByteArray();
//                                sendBuffer(bytes);
//                            }
//                        } else {
                            sendBuffer(buffer);
//                        }

                        if (msgSeqNumber == 256) {
                            msgSeqNumber = 1;
                        }
                    } catch (Exception e) {
                        take.onSendFailed(e);
                    }
                }
            } catch (InterruptedException ignored) {
            } finally {
                disconnect();
            }
        }
    };

    private Thread mTaskThread;

    /**
     * Establish a mavlink connection. If the connection is successful, it will
     * be reported through the MavLinkConnectionListener interface.
     */
    public synchronized void connect() {
//        Set<String> xbeeMacs = App.getApp().getConfig().getXbeeMacs();

        if (mConnectionStatus != MAVLINK_DISCONNECTED) {
            return;
        }
        mConnectionStatus = MAVLINK_CONNECTING;
        Thread taskThread = this.mTaskThread;
        if (taskThread != null && taskThread.isAlive()) {
            taskThread.interrupt();
        }

        reportConnectionStateChange(mConnectionStatus);
        connectHandlersSend.clear();
        connectHandlers.clear();
//        if (xbeeMacs != null)
//            for (String s : xbeeMacs) {
//                connectHandlers.add(new ConnectHandler(false, s));
//            }
        connectHandlersSend.addAll(connectHandlers);
        taskThread = new Thread(mConnectingTask, "MavLinkConnection-Connecting Thread");
        taskThread.start();
        mTaskThread = taskThread;
    }

    /**
     * Disconnect a mavlink connection. If the operation is successful, it will
     * be reported through the MavLinkConnectionListener interface.
     */
    public synchronized void disconnect() {
        if (mConnectionStatus == MAVLINK_DISCONNECTED)
            return;
        mConnectionStatus = MAVLINK_DISCONNECTED;
        currentMac = "";
        connectTimer.stop();
        Thread taskThread = this.mTaskThread;
        mTaskThread = null;
        if (taskThread != null) {
            taskThread.interrupt();
        }
        try {
            closeConnection();
        } catch (Exception e) {
//            reportComError(e);
        }

        reportConnectionStateChange(mConnectionStatus);
    }

    public int getConnectionStatus() {
        return mConnectionStatus;
    }

    /**
     * return false "Unable to send mavlink packet. Packet queue is full!"
     */
    public synchronized boolean sendMavPacket(ACKHandler ackHandler) {
        MAVLinkMessage message = ackHandler.getMessage();
        if (message.msgid != msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT)
            Log.i(message);
        return mPacketsToSend.offer(ackHandler);
    }

    /**
     * Adds a listener to the mavlink connection.
     *
     * @param listener Listener tag
     */
    public void setMavLinkConnectionListener(MavLinkConnectionListener listener) {
        mavLinkConnectionListener = listener;
//        if (listener != null && getConnectionStatus() == MAVLINK_CONNECTED) {
//            listener.onConnect();
//        }
    }


    protected abstract void openConnection() throws Exception;

    protected abstract int readDataBlock(byte[] buffer) throws IOException;

    protected abstract void sendBuffer(byte[] buffer) throws IOException;

    protected abstract void closeConnection() throws IOException;

    /**
     * @return The type of this mavlink connection.
     */
    public abstract int getConnectionType();

    /**
     * Utility method to notify the mavlink listeners about communication
     * errors.
     *
     * @param throwable
     */
    private void reportComError(final Throwable throwable) {
        Pool.submit(new Runnable() {
            @Override
            public void run() {
                MavLinkConnectionListener mavLinkConnectionListener = MavLinkConnection.this.mavLinkConnectionListener;
                if (mavLinkConnectionListener != null)
                    mavLinkConnectionListener.onComError(throwable);
            }
        });
    }

//    /**
//     * Utility method to notify the mavlink listeners about a successful
//     * connection.
//     */
//    private void reportConnect() {
//        if (mavLinkConnectionListener != null)
//            mavLinkConnectionListener.onConnect();
//    }

    /**
     * Utility method to notify the mavlink listeners about a connection
     * disconnect.
     */
    private void reportConnectionStateChange(final int state) {
        Pool.submit(new Runnable() {
            @Override
            public void run() {
                MavLinkConnectionListener mavLinkConnectionListener = MavLinkConnection.this.mavLinkConnectionListener;
                if (mavLinkConnectionListener != null)
                    mavLinkConnectionListener.onConnectionStateChange(state);
            }
        });
    }

    /**
     * Utility method to notify the mavlink listeners about received messages.
     *
     * @param packet received mavlink packet
     */
    private void reportReceivedPacket(final MAVLinkPacket packet) {
        Pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    MavLinkConnectionListener mavLinkConnectionListener = MavLinkConnection.this.mavLinkConnectionListener;
                    if (mavLinkConnectionListener != null)
                        mavLinkConnectionListener.onReceivePacket(packet);
                } catch (Exception e) {
                    Log.e("reportReceivedPacket", e);
                }
            }
        });
    }

}
