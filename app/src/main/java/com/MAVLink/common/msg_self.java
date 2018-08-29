package com.MAVLink.common;

import android.text.TextUtils;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.skylin.mavlink.model.Point;
import com.skylin.uav.drawforterrain.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import sjj.alog.Log;

/**
 * Created by zxy on 2016/9/18 0018.
 */
public class msg_self extends MAVLinkMessage {
    /**
     * 未收到RTK信号
     */
    public static final int RTK_VALUE_DISCONNECT = 0;
    /**
     * 有RTK无定位 单点
     */
    public static final int RTK_VALUE_LOCATING_FREE = 1;
    /**
     * 差分定位
     */
    public static final int RTK_VALUE_LOCATING_DIFF = 2;
    /**
     * 3无效
     */
    public static final int RTK_VALUE_LOCATING_INVALID = 3;
    /**
     * 固定解
     */
    public static final int RTK_VALUE_LOCATING_FIX = 4;
    /**
     * 浮动
     */
    public static final int RTK_VALUE_LOCATING_ALTER = 5;
    /**
     * 一键起飞 默认值//默认值99,0执行成功，1设置自稳错误，2安全开关解锁失败 3软解锁失败,4起飞失败. 在本次返航时会重置为99
     */
    public static final int ONE_KEY_NORMAL = 99;
    public static final byte ONE_KEY_SUCCESS = 0;
    public static final int ONE_KEY_SELF_POISE_FAILED = 1;
    public static final int ONE_KEY_SAFE_SWITCH_UNLOCK_FAILED = 2;
    public static final int ONE_KEY_SOFT_UNLOCK_FAILED = 3;
    public static final int ONE_KEY_TAKEOFF_FAILED = 4;
    /**
     * 任务完成
     */
    public static final int BACK_STATUS_COMPLETE = 0;
    /**
     * 手动返航
     */
    public static final int BACK_STATUS_MANUAL_OPERATION = 1;
    /**
     * 低电量
     */
    public static final int BACK_STATUS_LOW_BATTERY = 2;
    /**
     * 低药量
     */
    public static final int BACK_STATUS_FEWER_DOSAGE = 3;
    /**
     * 冗余返航
     */
    public static final int BACK_STATUS_RTK_LOSE = 4;
    /**
     * 遥控器接管
     */
    public static final int BACK_STATUS_TAKE_OVER = 5;

    public static final int PIX_STATUS_STOP = 1;
    public static final int PIX_STATUS_FLYING = 99;
    private static final int SELF_INSPECTION_PASS = 1;
    private static final int SELF_INSPECTION_NOT_PASS = 0;

//    typedef enum _RTKRYSTATU
//    {
//        RY_NOMAL=0,//
//        RY_WAIT,// ȴ rtk״̬ ָ
//        RY_RTKNEVEROK,//rtk״̬    ʧЧ
//        RY_POWRRTL,
//        RY_POWRLAND,
//
//    }RTKRYSTATU;

    public static final int RTK_RY_STATUS_NORMAL = 0;
    public static final int RTK_RY_STATUS_WAIT = 1;
    public static final int RTK_RY_STATUS_NEVEROK = 2;
    public static final int RTK_RY_STATUS_POWRRTL = 3;
    public static final int RTK_RY_STATUS_POWRLAND = 4;
    public static final byte BATTERY_CHIEF_LOSE = 0x01;
    public static final byte BATTERY_CHIEF_LOW = 0x01<<1;
    public static final byte BATTERY_CHIEF_OVERLOAD = 0x01<<2;
    public static final byte BATTERY_CHIEF_TEMPERATURE = 0x01<<3;
    public static final byte BATTERY_DRIVING_POWER = 0x01<<4;
    public static final byte BATTERY_CONTROL_SOURCE= 0x01<<5;
    public static final byte BATTERY_BACK= 0x01<<6;

    public static final int MAVLINK_MSG_ID_COMMAND_LONG = 195;
    public static final int MAVLINK_MSG_LENGTH = 18;
    private static final long serialVersionUID = MAVLINK_MSG_ID_COMMAND_LONG;
    public static final int READY_POWER_STATUS_FULL = 1;
    public static final int READY_POWER_STATUS_NORMAL = 2;
    public static final int READY_POWER_STATUS_LESS = 3;
    public static final int READY_POWER_STATUS_UNKNOWN = 110;
    /**
     * 当前返航状态(0任务完成，1手动返航，2低电量，3低药量)
     */
    private byte backStatus;
    /**
     * 飞机执行任务后下降状态(1已经降落在地面，电机已经锁定,99在空中)此值要在一键起飞命令执行成功后才有效
     */
    private byte pixstatu;
    /**
     * 当前药量
     */
    private short curdosage;
    /**
     * 当前电量 0电量不足，否则为当前电量 单位(V)
     */
    private float readypower;
    private byte readypowerStatus;
    private Point breakPoint;
    /**
     * rtk2真实状态 0未接上rtk,1有rtk无定位,1单点，2，差分定位，3无效,4固定解，5浮动解
     */
    private byte rtk;
    /**
     * 一键起飞命令执行状态。默认值99,0执行成功， 在本次飞机落地会重置为99,
     * //默认值99,0执行成功，1设置自稳错误，2安全开关解锁失败 3软解锁失败,4起飞失败. 在本次返航时会重置为99
     */
    private byte onekeyfly;
    /**
     * 错误状态 ER_NONE 0,
     * ER_RTKYAWBAD 1,//角度失效
     * ER_RTKBAD 2,//rtk暂时失效
     * ER_RTKLONGBAD 3,//rtk长时间不恢复
     */
    private byte errorstatu;
    private short missionSeq;
    /**
     * 1 自检通过
     */
    private long self_inspection;
    private byte rtkRyStatu;
    private byte battery_back;

    /**
     * Constructor for a new message, just initializes the msgid
     */

    private msg_self() {
        msgid = MAVLINK_MSG_ID_COMMAND_LONG;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     */
    public msg_self(MAVLinkPacket mavLinkPacket) {
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_COMMAND_LONG;
        unpack(mavLinkPacket.payload);
    }

    public byte getBackStatus() {
        return backStatus;
    }

    public String getBackStatusText() {
        return getBackStatusText(getBackStatus());
    }

    public static String getBackStatusText(byte value) {
        switch (value) {
            case msg_self.BACK_STATUS_COMPLETE:
                return "任务完成";
            case msg_self.BACK_STATUS_FEWER_DOSAGE:
                return "低药返航";
            case msg_self.BACK_STATUS_LOW_BATTERY:
                return "低电返航";
            case msg_self.BACK_STATUS_MANUAL_OPERATION:
                return "手动返航";
            case msg_self.BACK_STATUS_RTK_LOSE:
                return "冗余返航";
            case BACK_STATUS_TAKE_OVER:
                return "遥控器接管";
            default:
                return "未知返航状态码:" + value;
        }
    }

    public byte getPixstatu() {
        return pixstatu;
    }

    public short getCurdosage() {
        return curdosage;
    }

    public float getReadypower() {
        return readypower;
    }

    public float getReadypowerStatus() {
        return readypowerStatus;
    }

    public String getReadypowerStatusText() {
        switch (readypowerStatus) {
            case READY_POWER_STATUS_FULL:
                return "电量充足";
            case READY_POWER_STATUS_NORMAL:
                return "电量正常";
            case READY_POWER_STATUS_LESS:
                return "电量不足";
            case READY_POWER_STATUS_UNKNOWN:
                return String.valueOf(readypower);
        }
        return "unknown "+readypowerStatus;
    }

    public Point getBreakPoint() {
        return breakPoint;
    }

    public byte getRtk() {
        return rtk;
    }

    public String getRtkText() {
        return getRtkText(getRtk());
    }

    public static String getRtkText(int value) {
        switch (value) {
            case RTK_VALUE_DISCONNECT:
                return "RTK未连接";
            case RTK_VALUE_LOCATING_FREE:
                return "RTK单点";
            case RTK_VALUE_LOCATING_DIFF:
                return "RTK差分";
            case RTK_VALUE_LOCATING_INVALID:
                return "RTK无效";
            case RTK_VALUE_LOCATING_FIX:
                return "RTK固定";
            case RTK_VALUE_LOCATING_ALTER:
                return "RTK浮动";
            default:
                return "RTK未知状态码：" + value;
        }
    }

    public byte getOnekeyfly() {
        return onekeyfly;
    }

    public String getOneKeyFlyText() {
        return getOneKeyFlyText(getOnekeyfly());
    }

    public static String getOneKeyFlyText(byte value) {
        //默认值99,0执行成功，1设置自稳错误，2安全开关解锁失败 3软解锁失败,4起飞失败. 在本次返航时会重置为99
        switch (value) {
            case ONE_KEY_SAFE_SWITCH_UNLOCK_FAILED:
                return "安全开关解锁失败";
            case ONE_KEY_SELF_POISE_FAILED:
                return "设置自稳失败";
            case ONE_KEY_SOFT_UNLOCK_FAILED:
                return "软解锁失败";
            case ONE_KEY_TAKEOFF_FAILED:
                return "起飞失败";
            case ONE_KEY_SUCCESS:
                return "起飞成功";
            default:
                return "起飞失败"+value;
        }
    }

    public byte getErrorstatu() {
        return errorstatu;
    }

    public int getMissionSeq() {
        return missionSeq;
    }

    public long getSelf_inspection() {
        return self_inspection;
    }

    public boolean isChecked() {
        return isChecked(getSelf_inspection());
    }

    public static boolean isChecked(long self_inspection) {
        if (self_inspection == SELF_INSPECTION_PASS) {
            return true;
        }
        if (self_inspection == SELF_INSPECTION_NOT_PASS) {
            return false;
        }
        for (SELF_INSPECTION_ITEM item : SELF_INSPECTION_ITEM.values()) {
            if ((item.value & self_inspection) == 0) {
                return false;
            }
        }
        return true;
    }

    public String getSelf_inspectionText() {
        return getSelf_inspectionText(self_inspection);
    }

    public static String getSelf_inspectionText(long status) {
        if (status == SELF_INSPECTION_PASS) {
            return "自检通过";
        } else if (status == SELF_INSPECTION_NOT_PASS) {
            return "自检未通过";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            boolean pass = true;
            for (SELF_INSPECTION_ITEM item : SELF_INSPECTION_ITEM.values()) {
                String itemResult = getSelf_inspectionItemResult(item, status);
                if (!TextUtils.isEmpty(itemResult)) {
                    builder.append(itemResult);
                    builder.append(",");
                    pass = false;
                }
            }
            if (pass) {
                return "自检通过";
            } else {
                builder.deleteCharAt(builder.length() - 1);
                builder.append("]");
                return builder.toString();
            }
        }
    }

    private static String getSelf_inspectionItemResult(SELF_INSPECTION_ITEM item, long value) {
        return (value & item.value) == 0 ? item.name + ":fault" : "";
    }

    public byte getRtkRyStatu() {
        return rtkRyStatu;
    }

    public byte getBattery_back() {
        return battery_back;
    }

    public List<String> getBatteryStatusText() {
        return getBatteryStatusText(battery_back);
    }
    public static List<String> getBatteryStatusText(byte status) {
        List<String> strings = new ArrayList<>();
        if (checkBatteryError(status,BATTERY_CHIEF_LOSE)) {
            strings.add("主电池缺失");
        }
        if (checkBatteryError(status,BATTERY_CHIEF_LOW)) {
            strings.add("主电池电量低");
        }
        if (checkBatteryError(status,BATTERY_CHIEF_OVERLOAD)) {
            strings.add("主电池过流");
        }
        if (checkBatteryError(status,BATTERY_CHIEF_TEMPERATURE)) {
            strings.add("主电池温度过高");
        }
        if (checkBatteryError(status,BATTERY_DRIVING_POWER)) {
            strings.add("驱动电源错误");
        }
        if (checkBatteryError(status,BATTERY_CONTROL_SOURCE)) {
            strings.add("控制电源错误");
        }
        if (checkBatteryError(status,BATTERY_BACK)) {
            strings.add("备用电池");
        }
        return strings;
    }
    public static boolean checkBatteryError(byte battery_back,byte b) {
        return (battery_back&b)==b;
    }
    /**
     * Generates the payload for a mavlink message for a message of this type
     *
     * @return
     */
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_COMMAND_LONG;
//        packet.payload.putFloat(param1);
//        packet.payload.putFloat(param2);
//        packet.payload.putFloat(param3);
//        packet.payload.putFloat(param4);
//        packet.payload.putFloat(param5);
//        packet.payload.putFloat(param6);
//        packet.payload.putFloat(param7);
//        packet.payload.putShort(command);
//        packet.payload.putByte(target_system);
//        packet.payload.putByte(target_component);
//        packet.payload.putByte(confirmation);

        return packet;
    }

    /**
     * Decode a command_long message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        try {
            payload.resetIndex();
            this.readypower = payload.getFloat();
            breakPoint = new Point(payload.getInt() * 0.0000001, payload.getInt() * 0.0000001);
            this.curdosage = payload.getShort();
            this.missionSeq = payload.getShort();
            this.backStatus = payload.getByte();
            this.pixstatu = payload.getByte();
            this.rtk = payload.getByte();
            this.onekeyfly = payload.getByte();
            this.errorstatu = payload.getByte();
            this.self_inspection = payload.getByte();
            this.battery_back = payload.getByte();
            this.rtkRyStatu = payload.getByte();
            try {
                this.readypowerStatus = payload.getByte();
            } catch (Exception e) {
                this.readypowerStatus = READY_POWER_STATUS_UNKNOWN;
            }
        } catch (Exception e) {
            Log.e("自检消息解析异常", e);
            self_inspection = -1;
            ToastUtil.show("飞机状态消息解析出错:" + e.getMessage());
        }

    }

    @Override
    public String toString() {
        return "msg_self{" +
                "backStatus=" + backStatus +
                ", pixstatu=" + pixstatu +
                ", curdosage=" + curdosage +
                ", readypower=" + readypower +
                ", breakPoint=" + breakPoint +
                ", rtk=" + rtk +
                ", onekeyfly=" + onekeyfly +
                ", errorstatu=" + errorstatu +
                ", missionSeq=" + missionSeq +
                ", self_inspection=" + self_inspection +
                ", rtkRyStatu=" + rtkRyStatu +
                ", battery_back=" + getBatteryStatusText() +
                ", readypowerStatus=" + getReadypowerStatusText() +
                '}';
    }

    public enum SELF_INSPECTION_ITEM {
        BARO(1 << 1, "气压计"),
        RC(1 << 2, "遥控器"),
        COMPASS(1 << 3, "磁传感器"),
        INS(1 << 4, "惯导"),
        VOLTAGE(1 << 5, "电压"),
        LOGGING(1 << 6, "日志"),
        PARAMETERS(1 << 7, "参数"),
        GPS(1 << 8, "GPS"),
        FENCE(1 << 9, "围栏"),
        MOTOR(1 << 10, "电机");

        public final long value;
        public final String name;

        SELF_INSPECTION_ITEM(long value, String name) {
            this.value = value;
            this.name = name;
        }
    }
}
