package com.skylin.mavlink.handler;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.ardupilotmega.msg_mount_status;
import com.MAVLink.ardupilotmega.msg_prearm_flag;
import com.MAVLink.ardupilotmega.msg_user_status;
import com.MAVLink.ardupilotmega.msg_vip_events;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps2_raw;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_raw_imu;
import com.MAVLink.common.msg_rc_channels_raw;
import com.MAVLink.common.msg_servo_output_raw;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_RESULT;
import com.MAVLink.enums.MAV_SEVERITY;
import com.skylin.mavlink.MavLinkUAVConsole;
import com.skylin.mavlink.model.ApmModes;
import com.skylin.mavlink.model.CurrentMission;
import com.skylin.mavlink.model.GlobalPosition;
import com.skylin.mavlink.model.GpsState;
import com.skylin.mavlink.model.MsgEvent;
import com.skylin.mavlink.model.Parameter;
import com.skylin.mavlink.model.PrearmFlag;
import com.skylin.mavlink.model.UAV;
import com.skylin.mavlink.model.UavStatus;
import com.skylin.mavlink.model.Versions;
import com.skylin.mavlink.utils.Msg_statustextsKt;
import com.skylin.uav.drawforterrain.util.ToastUtil;

import sjj.alog.Log;

public class MavLinkMsgHandler {

    private static final byte SEVERITY_HIGH = 3;
    private static final byte SEVERITY_CRITICAL = 4;
    private UAV uav;
    private HeartBeatHandler beatHandler;
    private final MavLinkUAVConsole console;

    public MavLinkMsgHandler(UAV uav, HeartBeatHandler beatHandler, MavLinkUAVConsole console) {
        this.uav = uav;
        this.beatHandler = beatHandler;
        this.console = console;
    }

    public void receiveData(MAVLinkMessage msg) {
        switch (msg.msgid) {
            case msg_user_status.MAVLINK_MSG_ID_USER_STATUS:
                uav.setUavStatus(UavStatus.create((msg_user_status) msg));
                break;
            case msg_prearm_flag.MAVLINK_MSG_ID_PREARM_FLAG:
                uav.setPrearmFlag(PrearmFlag.create((msg_prearm_flag) msg));
                break;
            case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
                Parameter parameter = new Parameter((msg_param_value) msg);
                uav.putParameter(parameter);
                break;
            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE://姿态
                msg_attitude m_att = (msg_attitude) msg;
                //滚转角 俯仰角 偏航角
                uav.setRoll(Math.toDegrees(m_att.roll));
                uav.setPitch(Math.toDegrees(m_att.pitch));
                uav.setYaw(Math.toDegrees(m_att.yaw));
                break;
            case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
                msg_vfr_hud m_hud = (msg_vfr_hud) msg;
                //高度 地速 空速 爬升率
//                uav.setAltitude(m_hud.alt);
                uav.setGroundSpeed(m_hud.groundspeed);
                uav.setAirSpeed(m_hud.airspeed);
                uav.setClimbSpeed(m_hud.climb);
                break;
            case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT://当前任务代号:
                uav.setCurrentMission(CurrentMission.create((msg_mission_current) msg));
                break;
            case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
                // 导航控制器输出。主要用于在实际飞行之前检查控制的输出信号，以便调节
//                控制器内部参数
                msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
                //wp_dist 到下一任务点的距离 alt_error 高度差 aspd_error 速度差
                uav.setWpDist(m_nav.wp_dist);
                uav.setAltError(m_nav.alt_error);
                uav.setAspdError(m_nav.aspd_error);
                //nav_pitch 目标俯仰角 nav_roll 目标滚转角 nav_bearing 目标指向角
                uav.setNavPitch(m_nav.nav_pitch);
                uav.setNavRoll(m_nav.nav_roll);
                uav.setNavBearing(m_nav.nav_bearing);
                break;
            // 原始的姿态传感器信息
            case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
                msg_raw_imu msg_imu = (msg_raw_imu) msg;
                //地磁强度
                uav.setXmag(msg_imu.xmag);
                uav.setYmag(msg_imu.ymag);
                uav.setZmag(msg_imu.zmag);
                break;
            //heartbeat n. 心跳
            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
//                Log.i("frequency:"+frequency.count());
                msg_heartbeat msg_heart = (msg_heartbeat) msg;
                //飞行器类型
                uav.setType(msg_heart.type);
                //系统状态，参阅
                uav.setSystemStatus(msg_heart.system_status);
                uav.setBaseMode(msg_heart.base_mode);
                ApmModes newMode = ApmModes.getMode(msg_heart.custom_mode, msg_heart.type);
                uav.setMode(newMode);
                uav.setSysid((short) msg_heart.sysid);
                uav.setCompid((short) msg_heart.compid);
                beatHandler.onHeartbeat(msg_heart);
                break;
            //整型数表示的全球定位数据
            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
                uav.setGlobalPosition(GlobalPosition.create((msg_global_position_int) msg));
                break;
            //
            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
                msg_gps_raw_int gps = (msg_gps_raw_int) msg;
                uav.setGpsState(GpsState.create(gps));
//                uav.setPosition(new Point(gps.lon * 1.0 / 1E7, gps.lat * 1.0 / 1E7,gps.alt * 1.0 / 1000));
                break;
            case msg_gps2_raw.MAVLINK_MSG_ID_GPS2_RAW:
                uav.setGps2FixType(((msg_gps2_raw) msg).fix_type);
                break;
            case msg_ekf_status_report.MAVLINK_MSG_ID_EKF_STATUS_REPORT:
//			Util.log2("msg_ekf_status_report.MAVLINK_MSG_ID_EKF_STATUS " + ((msg_ekf_status_report) msg).compass_variance);
                uav.setEkfStatus(((msg_ekf_status_report) msg).compass_variance);
                break;

            case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW://舵机输出数据
//                drone.getRC().setRcInputValues((msg_rc_channels_raw) msg);
                break;
            case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW://舵机输出原始数据
//                drone.getRC().setRcOutputValues((msg_servo_output_raw) msg);
                break;
            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT://状态文本消息

                msg_statustext msg_statustext = (com.MAVLink.common.msg_statustext) msg;
                Log.e(msg_statustext.getText());
                if (msg_statustext.severity == MAV_SEVERITY.MAV_SEVERITY_NOTICE) {
                    uav.addWarning(Msg_statustextsKt.getWarning(msg_statustext));
                    break;
                }
                String message = msg_statustext.getText();
                if (msg_statustext.severity == SEVERITY_HIGH || msg_statustext.severity == SEVERITY_CRITICAL) {
//                    uav.setWarning(message);
                } else if (message.equals("Low Battery!")) {
//                    uav.setWarning(message);
                } else if (message.contains("ArduCopter")) {
                    uav.setFirmwareVersion(message);
                } else if (message.startsWith("TQ-")) {
                    uav.setVersion(new Versions(message));
                } else if (message.startsWith("Reached command #")) {
                    uav.setReachedCommand(Integer.parseInt(message.replace("Reached command #","")));
                }
                break;
            case msg_camera_feedback.MAVLINK_MSG_ID_CAMERA_FEEDBACK:
//                drone.getCamera().newImageLocation((msg_camera_feedback) msg);
                break;
            case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
//                drone.getCamera().updateMountOrientation(((msg_mount_status) msg));
                break;
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
                msg_command_ack ack = (msg_command_ack) msg;
                boolean accepted = ack.result == MAV_RESULT.MAV_RESULT_ACCEPTED;
                break;
            case msg_vip_events.MAVLINK_MSG_ID_VIP_EVENTS:
                msg_vip_events events = (msg_vip_events) msg;
                console.setEventAck(events.sequence);
                uav.setMsgEvent(MsgEvent.create(events));
                ToastUtil.show(MsgEvent.create(events).toString());
                Log.e(MsgEvent.create(events));
                break;
            default:
//                LogUtils.i("收到未处理消息[msgid:" + msg.msgid + "]");
                break;
        }
    }
}
