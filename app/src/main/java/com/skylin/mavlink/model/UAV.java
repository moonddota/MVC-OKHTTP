package com.skylin.mavlink.model;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.MAVLink.enums.MAV_MODE_FLAG;
import com.MAVLink.enums.MAV_STATE;
import com.MAVLink.enums.MAV_TYPE;
import com.skylin.mavlink.connection.MavLinkConnection;
import com.skylin.mavlink.utils.RXBUS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sjj.alog.Log;
import static com.MAVLink.common.msg_self.READY_POWER_STATUS_UNKNOWN;

/**
 * Created by SJJ on 2017/3/30.
 */

public class UAV {
    /**
     * 飞行速度
     */
    public static final String PARAM_WPNAV_SPEED = "WPNAV_SPEED";
    public static final String PARAM_WP_YAW_BEHAVIOR = "WP_YAW_BEHAVIOR";
    public static final String PARAM_TQ_SPARY_NUM = "TQ_SPARY_NUM";
    public static final String PARAM_TQ_STOPPOINT_ALT = "TQ_STOPPOINT_ALT";
    public static final String PARAM_TQ_STOPPOINT_LAT = "TQ_STOPPOINT_LAT";
    public static final String PARAM_TQ_STOPPOINT_LON = "TQ_STOPPOINT_LON ";
    public static final String PARAM_TQ_STOPPOINT_ID = "TQ_STOPPOINT_ID";
    public static final String PARAM_TQ_STOPOINT_SEQ = "TQ_STOPOINT_SEQ";
    /**
     * 返航高度
     */
    public static final String PARAM_RTL_ALT = "RTL_ALT";
    public static final String PARAM_GPS_RADIO_CHAN = "GPS_RADIO_CHAN";
    public static final String PARAM_ENA_RADIO = "ENA_RADIO";

    public static final int SYSTEM_STATE_UNINIT = 0; /* Uninitialized system, state is unknown. | */
    public static final int SYSTEM_STATE_BOOT = 1; /* System is booting up. | */
    public static final int SYSTEM_STATE_CALIBRATING = 2; /* System is calibrating and not flight-ready. | */
    public static final int SYSTEM_STATE_STANDBY = 3; /* System is grounded and on standby. It can be launched any time. | */
    public static final int SYSTEM_STATE_ACTIVE = 4; /* System is active and might be already airborne. Motors are engaged. | */
    public static final int SYSTEM_STATE_CRITICAL = 5; /* System is in a non-normal flight mode. It can however still navigate. | */
    public static final int SYSTEM_STATE_EMERGENCY = 6; /* System is in a non-normal flight mode. It lost control over parts or over the whole airframe. It is in mayday and going down. | */
    public static final int SYSTEM_STATE_POWEROFF = 7; /* System just initialized its power-down sequence, will shut down now. | */
    public static final int SYSTEM_STATE_ENUM_END = 8; /*  | */

    public static final int CONNECTED = MavLinkConnection.MAVLINK_CONNECTED;
    public static final int CONNECTING = MavLinkConnection.MAVLINK_CONNECTING;
    public static final int DISCONNECTED = MavLinkConnection.MAVLINK_DISCONNECTED;
    private int connectStateCode = DISCONNECTED;


    private short sysid = 1;
    private short compid = 1;
    private final HashMap<String, Parameter> parameters = new HashMap<>();
    /**
     * 地面速度
     */
    private float groundSpeed;
    /**
     * 飞行速度
     */
    private float airSpeed;
    /**
     * 爬升速率
     */
    private float climbSpeed;
    /**
     * 滚转角
     */
    private double roll;
    /**
     * 俯仰角
     */
    private double pitch;
    /**
     * 偏航角
     */
    private double yaw;
    /**
     * 到下一任务点的距离
     */
    private int wpDist;
    /**
     * 高度差
     */
    private float altError;
    /**
     * 速度差
     */
    private float aspdError;
    /**
     * 目标俯仰角
     */
    private float navPitch;
    /**
     * 目标滚转角
     */
    private float navRoll;
    /**
     * 目标指向角
     */
    private short navBearing;
    /**
     * 地磁强度 X
     */
    private short xmag;
    /**
     * 地磁强度 Y
     */
    private short ymag;
    /**
     * 地磁强度 Z
     */
    private short zmag;
    /**
     * {@link MAV_TYPE} 飞行器类型
     */
    private short type;
    private boolean selfInspectionStatus;
    /**
     * 系统状态，参阅
     * {@link #SYSTEM_STATE_ACTIVE}
     * {@link #SYSTEM_STATE_BOOT}
     * {@link #SYSTEM_STATE_CALIBRATING}
     * {@link #SYSTEM_STATE_CRITICAL}
     * {@link #SYSTEM_STATE_EMERGENCY}
     * {@link #SYSTEM_STATE_POWEROFF}
     * {@link #SYSTEM_STATE_ENUM_END}
     * {@link #SYSTEM_STATE_STANDBY}
     * {@link #SYSTEM_STATE_UNINIT}
     */
    private short systemStatus;
    private short baseMode;
    private ApmModes mode;
    private Point position;
    private short gps2FixType;
    private float ekfStatus;
    private LinkedList<String> warning = new LinkedList<>();
    private String firmwareVersion;
    /**
     * 当前药量 1有药，0无药
     */
    private short curdosage;
    /**
     * 当前电量 0电量不足，否则为当前电量 单位(V)
     */
    private float readypower;

    private Versions version;

    public static final int INIT_NOT = -1;
    public static final int INIT_PROCESSING = 0;
    public static final int INIT_COMPLETE = 1;
    private int initStatus = INIT_NOT;
    private int reachedCommand = -1;
    private GlobalPosition globalPosition;
    private PrearmFlag prearmFlag;
    private UavStatus uavStatus;
    private MsgEvent msgEvent;
    public GpsState gpsState = new GpsState();
    private CurrentMission currentMission;

    /**
     * 飞机连接状态
     *
     * @param stateCode {@link #DISCONNECTED}
     *                  {@link #CONNECTING}
     *                  {@link #CONNECTED}
     */
    public synchronized void setConnectionState(int stateCode) {
        if (this.connectStateCode == stateCode) return;
        int lastConnectionState = this.connectStateCode;
        this.connectStateCode = stateCode;
        switch (stateCode) {
            case DISCONNECTED:
                resetUAV();
                sendEvent(UavAttribute.connectionState_disconnected, lastConnectionState == CONNECTED ? "连接已断开" : "连接失败");
                break;
            case CONNECTED:
                sendEvent(UavAttribute.connectionState_connected);
                break;
            case CONNECTING:
                sendEvent(UavAttribute.connectionState_connecting);
                break;
                default:break;
        }
    }

    private void resetUAV() {
        setInitStatus(INIT_NOT);
        parameters.clear();
        setGpsState(new GpsState());
        warning.clear();
    }

    /**
     * 飞机连接状态
     * <p>
     * {@link #DISCONNECTED}
     * {@link #CONNECTING}
     * {@link #CONNECTED}
     */
    public synchronized int getConnectionState() {
        return connectStateCode;
    }

    public boolean isConnected() {
        return connectStateCode == CONNECTED;
    }

    public short getSysid() {
        return sysid;
    }

    public void setSysid(short sysid) {
        this.sysid = sysid;
    }

    public short getCompid() {
        return compid;
    }

    public void setCompid(short compid) {
        this.compid = compid;
    }

    public Parameter getParameters(String parameterName) {
        for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            if (parameterName.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void putParameter(Parameter parameter) {
        Parameter parameter1 = parameters.get(parameter.name);
        if (parameter1 != null && parameter1.getValue() == parameter.getValue()) {
            return;
        }
        parameters.put(parameter.name, parameter);
        sendEvent(UavAttribute.parameter);
    }


    public void setRoll(double roll) {
        if (this.roll != roll) {
            this.roll = roll;
            sendEvent(UavAttribute.roll);
        }
    }

    public double getRoll() {
        return roll;
    }

    public void setPitch(double pitch) {
        if (this.pitch == pitch) return;
        this.pitch = pitch;
        sendEvent(UavAttribute.pitch);
    }

    public double getPitch() {
        return pitch;
    }

    /**
     * 偏航角
     *
     * @param yaw
     */
    public void setYaw(double yaw) {
        if (Math.abs(this.yaw - yaw) < 0.1) return;
        this.yaw = yaw;
        sendEvent(UavAttribute.yaw, yaw);
    }

    public double getYaw() {
        return yaw;
    }

    public void setGroundSpeed(float groundSpeed) {
        if (this.groundSpeed == groundSpeed) return;
        this.groundSpeed = groundSpeed;
        sendEvent(UavAttribute.groundSpeed);
    }

    public float getGroundSpeed() {
        return groundSpeed;
    }

    public void setAirSpeed(float airSpeed) {
        if (this.airSpeed == airSpeed) return;
        this.airSpeed = airSpeed;
        sendEvent(UavAttribute.airSpeed);
    }

    public void setClimbSpeed(float climbSpeed) {
        if (this.climbSpeed == climbSpeed) return;
        this.climbSpeed = climbSpeed;
        sendEvent(UavAttribute.climbSpeed);
    }

    public void setWpDist(int wpDist) {
        if (this.wpDist == wpDist) return;
        this.wpDist = wpDist;
        sendEvent(UavAttribute.wpDist);
    }

    public int getWpDist() {
        return wpDist;
    }


    public void setAltError(float altError) {
        if (this.altError == altError) return;
        this.altError = altError;
        sendEvent(UavAttribute.altError);
    }

    public float getAltError() {
        return altError;
    }

    public void setAspdError(float aspdError) {
        if (this.aspdError == aspdError) return;
        this.aspdError = aspdError;
        sendEvent(UavAttribute.aspdError);
    }

    public float getAspdError() {
        return aspdError;
    }

    public void setNavPitch(float navPitch) {
        if (this.navPitch == navPitch) return;
        this.navPitch = navPitch;
        sendEvent(UavAttribute.navPitch);
    }

    public float getNavPitch() {
        return navPitch;
    }

    public void setNavRoll(float navRoll) {
        if (this.navRoll == navRoll) return;
        this.navRoll = navRoll;
        sendEvent(UavAttribute.navRoll);
    }

    public float getNavRoll() {
        return navRoll;
    }

    public void setNavBearing(short navBearing) {
        if (this.navBearing == navBearing) return;
        this.navBearing = navBearing;
        sendEvent(UavAttribute.navBearing);
    }

    public short getNavBearing() {
        return navBearing;
    }

    public void setXmag(short xmag) {
        if (this.xmag == xmag) return;
        this.xmag = xmag;
        sendEvent(UavAttribute.xmag);
    }

    public short getXmag() {
        return xmag;
    }

    public void setYmag(short ymag) {
        if (this.ymag == ymag) return;
        this.ymag = ymag;
        sendEvent(UavAttribute.ymag);
    }

    public short getYmag() {
        return ymag;
    }

    public void setZmag(short zmag) {
        if (this.zmag == zmag) return;
        this.zmag = zmag;
        sendEvent(UavAttribute.zmag);
    }

    public short getZmag() {
        return zmag;
    }

    public void setType(short type) {
        if (this.type == type) return;
        this.type = type;
        sendEvent(UavAttribute.type);
    }

    public short getType() {
        return type;
    }

    public boolean getSelfInspectionStatus() {
        return selfInspectionStatus;
    }

    public void setSelfInspectionStatus(boolean selfInspectionStatus) {
        if (this.selfInspectionStatus != selfInspectionStatus) {
            this.selfInspectionStatus = selfInspectionStatus;
            sendEvent(UavAttribute.selfInspectionStatus, selfInspectionStatus);
        }
    }

    public void setSystemStatus(short systemStatus) {
        if (this.systemStatus == systemStatus) return;
        this.systemStatus = systemStatus;
        sendEvent(UavAttribute.systemStatus);
    }

    public short getSystemStatus() {
        return systemStatus;
    }

    public String getSystemStatusText(short state) {
        switch (state) {
            case UAV.SYSTEM_STATE_UNINIT:
                return "未初始化";
            case UAV.SYSTEM_STATE_ACTIVE:
                return "正在运行";
            case UAV.SYSTEM_STATE_BOOT:
                return "正在启动";
            case UAV.SYSTEM_STATE_CALIBRATING:
                return "正在校对";
            case UAV.SYSTEM_STATE_CRITICAL:
                return "状态异常";
            case UAV.SYSTEM_STATE_STANDBY:
                return "准备就绪";
            case UAV.SYSTEM_STATE_EMERGENCY:
                return "危险降落";
            case UAV.SYSTEM_STATE_POWEROFF:
                return "即将关闭";
            default:
                return "unknown";
        }
    }

    public boolean isFlying() {
        return systemStatus == MAV_STATE.MAV_STATE_ACTIVE;
    }

    public void setBaseMode(short baseMode) {
        if (this.baseMode == baseMode) return;
        this.baseMode = baseMode;
        sendEvent(UavAttribute.baseMode);
    }

    public short getBaseMode() {
        return baseMode;
    }

    public boolean isArmed() {
        return (baseMode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED;
    }

    public void setMode(ApmModes mode) {
        if (mode.equals(this.mode)) return;
        this.mode = mode;
        sendEvent(UavAttribute.mode, mode);
    }

    public ApmModes getMode() {
        return mode;
    }

    public void setPosition(Point position) {
        if (position.equals(this.position)) {
            return;
        }
        if (this.position != null && this.position.distance(position) > 0.2)
            sendEvent(UavAttribute.position, position);
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public void setGlobalPosition(GlobalPosition globalPosition) {
        if (globalPosition.equals(this.globalPosition)) return;
        this.globalPosition = globalPosition;
        sendEvent(UavAttribute.globalPosition, position);
        setYaw(globalPosition.getYaw());
        setPosition(globalPosition.getPoint());
    }

    public GlobalPosition getGlobalPosition() {
        return globalPosition;
    }


    public void setGpsState(GpsState gpsState) {
        if (gpsState.equals(this.gpsState)) return;
        this.gpsState = gpsState;
        sendEvent(UavAttribute.gpsState, gpsState);
    }

    public void setGps2FixType(short gps2FixType) {
        if (this.gps2FixType == gps2FixType) return;
        this.gps2FixType = gps2FixType;
        sendEvent(UavAttribute.gps2FixType);
    }

    public short getGps2FixType() {
        return gps2FixType;
    }

    public void setEkfStatus(float ekfStatus) {
        if (this.ekfStatus == ekfStatus) return;
        this.ekfStatus = ekfStatus;
        sendEvent(UavAttribute.ekfStatus);
    }

    public float getEkfStatus() {
        return ekfStatus;
    }

    public void addWarning(String warning) {
        this.warning.addFirst(warning);
        sendEvent(UavAttribute.warning, warning);
    }

    public List<String> getWarning() {
        return warning;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setVersion(Versions version) {
        this.version = version;
        sendEvent(UavAttribute.version, version);
    }


    public Versions getVersion() {
        return version;
    }

    public short getCurdosage() {
        return curdosage;
    }

    public void setCurdosage(short curdosage) {
        if (this.curdosage == curdosage) {
            return;
        }
        this.curdosage = curdosage;
        sendEvent(UavAttribute.curdosage, curdosage);
    }

    public float getReadypower() {
        return readypower;
    }

    public void setReadypower(float readypower) {
        if (this.readypower != readypower) {
            this.readypower = readypower;
            sendEvent(UavAttribute.readypower, readypower);
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<AttributeChangeListener> listeners = new HashSet<>();

    public void sendEvent(@NonNull UavAttribute attribute) {
        sendEvent(attribute, null);
    }

    public synchronized void sendEvent(@NonNull final UavAttribute attribute, final Object o) {
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            for (AttributeChangeListener listener : listeners) {
                try {
                    listener.onChange(attribute, o);
                } catch (Exception e) {
                    Log.e("UAV sendEvent", e);
                }
            }
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (AttributeChangeListener listener : listeners) {
                        try {
                            listener.onChange(attribute, o);
                        } catch (Exception e) {
                            Log.e("UAV sendEvent", e);
                        }
                    }
                }
            });
        }

    }

    private void bus(Object o) {
        RXBUS.def.push(o);
    }

    public synchronized void addAttributeChangeListener(@NonNull AttributeChangeListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeAttributeChangeListener(AttributeChangeListener listener) {
        listeners.remove(listener);
    }

    public synchronized void clearAttributeChangeListener() {
        listeners.clear();
    }

    public void setInitStatus(int initStatus) {
        if (initStatus == this.initStatus) return;
        this.initStatus = initStatus;
        sendEvent(initStatus == INIT_COMPLETE ? UavAttribute.initStatus_complete : initStatus == INIT_PROCESSING ? UavAttribute.initStatus_processing : UavAttribute.initStatus_not, initStatus);
    }

    public boolean isInitComplete() {
        return initStatus == INIT_COMPLETE;
    }


    public void setReachedCommand(int reachedCommand) {
        this.reachedCommand = reachedCommand;
        sendEvent(UavAttribute.reachedCommand, reachedCommand);
    }

    public int getReachedCommand() {
        return reachedCommand;
    }

    public void setPrearmFlag(PrearmFlag prearmFlag) {
        bus(prearmFlag);
        if (prearmFlag.equals(this.prearmFlag)) {
            return;
        }
        this.prearmFlag = prearmFlag;
        sendEvent(UavAttribute.prearmFlag, prearmFlag);
        setSelfInspectionStatus(prearmFlag.isChecked());
    }

    public PrearmFlag getPrearmFlag() {
        return prearmFlag;
    }

    public void setUavStatus(UavStatus uavStatus) {
        bus(uavStatus);
        if (uavStatus.equals(this.uavStatus)) return;
        this.uavStatus = uavStatus;
        sendEvent(UavAttribute.uavStatus, uavStatus);
        setReadypower(uavStatus.getVoltage());
        setCurdosage(uavStatus.getLequidlevel());
    }

    public UavStatus getUavStatus() {
        return uavStatus;
    }

    public void setMsgEvent(MsgEvent msgEvent) {
        bus(msgEvent);
        if (msgEvent.equals(this.msgEvent)) {
            return;
        }
        this.msgEvent = msgEvent;
        sendEvent(UavAttribute.msgEvent, msgEvent);
    }

    public MsgEvent getMsgEvent() {
        return msgEvent;
    }

    public void setCurrentMission(CurrentMission currentMission) {
        if (currentMission.equals(this.currentMission) || !currentMission.isValid()) return;
        this.currentMission = currentMission;
        sendEvent(UavAttribute.currentMission,currentMission);
    }

    public CurrentMission getCurrentMission() {
        return currentMission;
    }

    public interface AttributeChangeListener {
        void onChange(UavAttribute attribute, Object o);
    }
}
