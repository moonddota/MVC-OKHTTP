package com.skylin.mavlink.handler;

import android.support.annotation.NonNull;

import com.MAVLink.common.msg_heartbeat;
import com.skylin.mavlink.ACKHandler;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.handler.ACK.DefaultACKHandler;
import com.skylin.mavlink.message.MavLinkMessageHeartbeat;
import com.skylin.mavlink.utils.Timer;

public class HeartBeatHandler {

    private static final long HEARTBEAT_NORMAL_TIMEOUT = 5000; //ms
    private static final long HEARTBEAT_TIME = 1000;
    public static final int INVALID_MAVLINK_VERSION = -1;
    private final ACKHandler msg;

    /**
     * Stores the version of the mavlink protocol.
     */
    private short mMavlinkVersion = INVALID_MAVLINK_VERSION;
    @NonNull
    private HeartbeatCallBack heartbeatCallBack;

    public HeartBeatHandler(@NonNull HeartbeatCallBack heartbeatCallBack) {
        this.heartbeatCallBack = heartbeatCallBack;
        msg = new DefaultACKHandler<>(heartbeatCallBack, MavLinkMessageHeartbeat.sMsg);
    }

    /**
     * @return the version of the mavlink protocol.
     */
    public short getMavlinkVersion() {
        return mMavlinkVersion;
    }

    public void onHeartbeat(msg_heartbeat msg) {
        mMavlinkVersion = msg.mavlink_version;
        watchdog.start(HEARTBEAT_NORMAL_TIMEOUT);
    }


    public synchronized void notifyConnected() {
        watchdog.start(HEARTBEAT_NORMAL_TIMEOUT);
        heartBeatTimer.start(0, HEARTBEAT_TIME);
    }


    public synchronized void notifyDisconnected() {
        watchdog.stop();
        heartBeatTimer.stop();
        mMavlinkVersion = INVALID_MAVLINK_VERSION;
    }

    private Timer heartBeatTimer = new Timer(new Runnable() {
        @Override
        public void run() {
            heartbeatCallBack.send(msg);
        }
    });
    private Timer watchdog = new Timer(new Runnable() {
        @Override
        public void run() {
            heartbeatCallBack.onHeartbeatTimeout();
        }
    });

    public interface HeartbeatCallBack extends SendMessage {
        void onHeartbeatTimeout();
    }
}
