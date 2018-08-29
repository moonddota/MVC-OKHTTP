package com.skylin.mavlink;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.skylin.mavlink.connection.MavLinkConnection;
import com.skylin.mavlink.connection.MavLinkConnectionListener;
import com.skylin.mavlink.exception.NoUavException;
import com.skylin.mavlink.exception.USBFailedException;
import com.skylin.mavlink.exception.USBNotFoundException;
import com.skylin.mavlink.exception.USBRejectException;
import com.skylin.mavlink.handler.HeartBeatHandler;
import com.skylin.mavlink.handler.MavLinkMsgHandler;
import com.skylin.mavlink.listener.AbstractACKListener;
import com.skylin.mavlink.model.Response;
import com.skylin.mavlink.model.UAV;
import com.skylin.mavlink.utils.Timer;
import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sjj.alog.Log;

/**
 * Created by SJJ on 2017/3/30.
 */

public class MavLinkUavClient implements SendMessage, MavLinkConnectionListener, HeartBeatHandler.HeartbeatCallBack {
    private static final int ACK_HANDLER_TIME = 2000;
    private final UAV uav = new UAV();
    private MavLinkUAVConsole console = new MavLinkUAVConsole(uav, this);
    private final Set<ACKHandler> ackHandlers = new HashSet<>();
    private HeartBeatHandler beatHandler = new HeartBeatHandler(this);
    private final MavLinkMsgHandler handler = new MavLinkMsgHandler(uav, beatHandler, console);
    public MavLinkConnection connection;

    public MavLinkUavClient(MavLinkConnection connection) {
        this.connection = connection;
    }

    public UAV getUav() {
        return uav;
    }

    public MavLinkUAVConsole getConsole() {
        return console;
    }

    public int getConnectionType() {
        return connection.getConnectionType();
    }

    public synchronized void connect() {
        if (connection.getConnectionStatus() == MavLinkConnection.MAVLINK_DISCONNECTED) {
            connection.setMavLinkConnectionListener(this);
            connection.connect();
            ackHandlerWatchdogTimer.start(ACK_HANDLER_TIME, ACK_HANDLER_TIME);
            connectWatchdog.start(20000);
        }
    }

    public synchronized void disconnect() {
        if (connection.getConnectionStatus() != MavLinkConnection.MAVLINK_DISCONNECTED) {
            connection.disconnect();
            ackHandlerWatchdogTimer.stop();
            ackHandlers.clear();
        }

    }

    public boolean isConnect() {
        return uav.getConnectionState() == UAV.CONNECTED;
    }

    public <T> boolean send(ACKHandler<T> ackHandler) {
        if (connection.getConnectionStatus() != MavLinkConnection.MAVLINK_CONNECTED) {
            ackHandler.response(new Response<T>().setSuccess(false).setErrorMessage(APP.getContext().getString(R.string.UAVActivity_tv13)));
            return false;
        }
        boolean b = connection.sendMavPacket(ackHandler);
        if (b && ackHandler.ack()) {
            synchronized (ackHandlers) {
                if (!ackHandlers.contains(ackHandler))
                    ackHandlers.add(ackHandler);
            }
            ackHandler.onSend();
        }
        if (!b) {
            ackHandler.onSendFailed(new Exception("发送失败"));
        }
        return b;
    }

    private Timer connectWatchdog = new Timer(new Runnable() {
        @Override
        public void run() {
            if (connection.getConnectionStatus() != MavLinkConnection.MAVLINK_CONNECTED) {
                disconnect();
            }
        }
    });

    private Timer ackHandlerWatchdogTimer = new Timer(new Runnable() {
        @Override
        public void run() {
            if (!ackHandlers.isEmpty())
                synchronized (ackHandlers) {
                    List<ACKHandler> timeouts = new ArrayList<>();
                    for (ACKHandler handler : ackHandlers) {
                        if (handler.timeout()) {
                            if (handler.retry()) {
                                handler.requestAck();
                            } else {
                                timeouts.add(handler);
                            }
                        }
                    }
                    ackHandlers.removeAll(timeouts);
                }
        }
    });

    @Override
    public void onReceivePacket(MAVLinkPacket packet) {
        MAVLinkMessage message = packet.unpack();

        if (message.msgid != msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT)
            Log.i(message);


        synchronized (handler) {
            handler.receiveData(message);
        }

        if (!ackHandlers.isEmpty())
            synchronized (ackHandlers) {
                List<ACKHandler> receive = new ArrayList<>();
                for (ACKHandler handler : ackHandlers) {
                    if (handler.handleMsg(message)) {
                        receive.add(handler);
                    }
                }
                ackHandlers.removeAll(receive);
            }

    }

    @Override
    public void onConnectionStateChange(int state) {
        uav.setConnectionState(state);
        if (MavLinkConnection.MAVLINK_CONNECTED == state) {
            beatHandler.notifyConnected();
            connectWatchdog.stop();
            console.requestData(true);
            console.readVersion(null);
//            console.requestpRrearmFlag();
        } else if (MavLinkConnection.MAVLINK_DISCONNECTED == state) {
            beatHandler.notifyDisconnected();
            connectWatchdog.stop();
            synchronized (ackHandlers) {
                for (ACKHandler handler : ackHandlers) {
                    handler.onDisconnect();
                }
                ackHandlers.clear();
            }
        }
    }

    @Override
    public void onComError(Throwable throwable) {
        Log.e("onComError:", throwable);
        String msg;
        if (throwable instanceof USBFailedException) {
            msg = "USB权限获取失败";
        } else if (throwable instanceof USBNotFoundException) {
            msg = "USB设备未找到";
        } else if (throwable instanceof USBRejectException) {
            msg = "USB权限被拒绝";
        } else if (throwable instanceof NoUavException) {
            msg = "未设置要连接的飞机";
        } else {
            msg = "Error:" + throwable.getMessage();
        }
        ToastUtil.show(msg);
    }

    @Override
    public void onHeartbeatTimeout() {
        disconnect();
    }

}
