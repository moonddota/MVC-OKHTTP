package com.skylin.mavlink

import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.ardupilotmega.msg_event_ack
import com.MAVLink.ardupilotmega.msg_prearm_flag
import com.MAVLink.common.msg_attitude
import com.MAVLink.common.msg_command_ack
import com.MAVLink.common.msg_command_long
import com.MAVLink.common.msg_heartbeat
import com.MAVLink.common.msg_mission_ack
import com.MAVLink.common.msg_mission_clear_all
import com.MAVLink.common.msg_rc_channels
import com.skylin.mavlink.message.msg_mission_item_servo
import com.MAVLink.common.msg_param_request_list
import com.MAVLink.common.msg_param_request_read
import com.MAVLink.common.msg_param_set
import com.MAVLink.common.msg_param_value
import com.MAVLink.common.msg_request_data_stream
import com.MAVLink.common.msg_self
import com.MAVLink.common.msg_set_mode
import com.MAVLink.common.msg_sys_status
import com.MAVLink.enums.MAV_CMD
import com.MAVLink.enums.MAV_DATA_STREAM
import com.MAVLink.enums.MAV_MODE
import com.MAVLink.enums.MAV_RESULT
import com.skylin.mavlink.handler.ACK.*
import com.skylin.mavlink.handler.ack.*
import com.skylin.mavlink.model.ApmModes
import com.skylin.mavlink.model.IntegerMap
import com.skylin.mavlink.model.Point
import com.skylin.mavlink.model.Progress
import com.skylin.mavlink.model.Response
import com.skylin.mavlink.model.Task
import com.skylin.mavlink.model.UAV
import com.skylin.mavlink.model.Versions
import com.skylin.mavlink.utils.*

import java.util.HashMap

/**
 * Created by SJJ on 2017/4/1.
 * UAV 控制
 */

class MavLinkUAVConsole(private val uav: UAV, private val sendMessage: SendMessage) {

    fun reboot(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_PREFLIGHT_REBOOT_SHUTDOWN, 1f)
    }

    fun setCapacityPumpSprayerSpeed(capacity: Float, pump: Float, sprayer: Float, speed: Float, listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, 443, capacity, sprayer, pump, speed)
    }

    fun testMotor(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, 209)
    }

    fun testSpray(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, 440, 1500f)
    }

    fun rinse(listener: ACKListener<msg_command_ack>, open: Boolean) {
        val msg = msg_command_long()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.command = 464
        msg.param1 = (if (open) 1 else 2).toFloat()
        val handler = CommandLongACKHandler(sendMessage, msg, listener)
        handler.retry = 0
        setCommandLong(handler)
    }

    fun selfInspectionSkip(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, 450, 11f)
    }

    fun missionStart(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_MISSION_START)
    }

    fun setArmed(isArm: Boolean, listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, if (isArm) 1f else 0f)
    }

    fun setLiquidLevel(isArm: Boolean, listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, if (isArm) 456 else 455, if (isArm) 9.2f else 8.2f)
    }

    fun readYAW(listener: ACKListener<Float>) {//// TODO: 2017/5/5 读取方向角 飞控没有返回方向角
        val msg = msg_command_long()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.command = 458
        msg.param1 = 13.2f
        sendMessage.send(ReadYAWACKHandler(sendMessage, msg, listener))
    }

    fun requestData(request: Boolean) {
        requestData(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS, if (request) 1 else 0, msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS)
        requestData(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1, if (request) 1 else 0, msg_attitude.MAVLINK_MSG_ID_ATTITUDE)
        //        requestData(MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, request ? 2 : 0, msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT, null);//position
    }

    fun requestpRrearmFlag() {
//        val msg = msg_request_data_stream()
//        msg.target_system = uav.sysid
//        msg.target_component = uav.compid
//        msg.req_message_rate = 2
//        msg.req_stream_id = MAV_DATA_STREAM.STREAM_PREARM_ERROR_FLAG.toShort()
//        msg.start_stop = 1
//        sendMessage.send(PrearmFlagHandler(sendMessage,msg,listener))
        requestData(MAV_DATA_STREAM.MAV_DATA_STREAM_PREARM_ERROR_FLAG, 1, msg_prearm_flag.MAVLINK_MSG_ID_PREARM_FLAG)
    }


    fun startCompassCalibration(listener: ACKListener<MAVLinkMessage>): CompassCalibrationHandler {
        val msg = msg_request_data_stream()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.req_message_rate = 10
        msg.req_stream_id = MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3.toShort()
        msg.start_stop = 1
        val calibrationAccelerometer = CompassCalibrationHandler(sendMessage, msg, listener)
        sendMessage.send(calibrationAccelerometer)
        return calibrationAccelerometer
    }

    fun startRemoteControlCalibration(listener: ACKListener<msg_rc_channels>): RemoteControlCalibrationHandler {
        val msg = msg_request_data_stream()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.req_message_rate = 10
        msg.req_stream_id = MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS.toShort()
        msg.start_stop = 1
        val handler = RemoteControlCalibrationHandler(sendMessage, msg, listener)
        sendMessage.send(handler)
        return handler
    }

    fun requestData(stream_id: Int, rate: Int, targetMsg: Int): Boolean {
        val msg = msg_request_data_stream()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.req_message_rate = rate.toShort().toInt()
        msg.req_stream_id = stream_id.toByte().toShort()
        msg.start_stop = (if (rate > 0) 1 else 0).toShort()
        val handler = MsgRequestDataStreamHandler(sendMessage, targetMsg, msg)
        return sendMessage.send(handler)
    }

    fun clearMission(listener: ACKListener<msg_mission_ack>): Boolean {
        val msg = msg_mission_clear_all()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        return sendMessage.send(CommonACKHandler(sendMessage, msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK, msg, listener))
    }

    fun hello(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, 465)
    }

    fun spray(open: Boolean, listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_DO_SET_SERVO, msg_mission_item_servo.spray_channel.toFloat(), if (open) msg_mission_item_servo.spray_pwm_maximum.toFloat() else msg_mission_item_servo.spray_pwm_close.toFloat())
    }

    fun battery_back(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, 462)
    }

    fun landing(listener: ACKListener<msg_command_ack>): Boolean {
        val msg = msg_set_mode()
        msg.target_system = uav.sysid
        msg.base_mode = 1
        msg.custom_mode = ApmModes.ROTOR_LAND.number.toLong()
        return sendMessage.send(CommonACKHandler(sendMessage, msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK, msg, listener))
    }

    /**
     * 返航
     *
     * @param listener
     * @return
     */
    fun goHome(listener: ACKListener<msg_command_ack>): Boolean {
        return setCommandLong(listener, MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH, 1.2f)
    }

    /**
     * 一键起飞
     *
     * @param responseListener
     * @return
     */
    fun oneKeyFly(responseListener: ACKListener<msg_command_ack>, hasRoadblock: Boolean, firstWayIndex: Int): Boolean {
        val msg = msg_command_long()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
//        msg.command = 457
        msg.command = 22
        msg.param1 = (if (hasRoadblock) 6 else 0).toFloat()
        msg.param2 = firstWayIndex.toFloat()
//        val handler = OneKeyFlyACKHandler(sendMessage, msg, responseListener)
        return setCommandLong(msg, responseListener)
        //        return setCommandLong(responseListener, 457, hasRoadblock ? 6 : 0);
    }

    /**
     * 悬停
     */
    fun setHoveringFlight(listener: ACKListener<msg_command_ack>, stop: Boolean): Boolean {
        return setCommandLong(listener, if (stop) MAV_CMD.MAV_CMD_STOP else MAV_CMD.MAV_CMD_CONTINUE)
    }

    fun setLoiter(listener: ACKListener<msg_command_ack>): Boolean {
        return setCommandLong(listener, MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM)
    }

    fun setLand(listener: ACKListener<msg_command_ack>): Boolean {
        return setCommandLong(listener, MAV_CMD.MAV_CMD_NAV_LAND)
    }

    fun setAuto(listener: ACKListener<msg_command_ack>): Boolean {
        val msg = msg_set_mode()
        msg.target_system = uav.sysid
        msg.base_mode = 1
        msg.custom_mode = ApmModes.ROTOR_AUTO.number.toLong()
        return sendMessage.send(CommonACKHandler(sendMessage, msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK, msg, listener))
    }

    fun setMode(listener: ACKListener<msg_command_ack>, mode: Int, custom: Int = 0, subMode: Int = 0): Boolean {
        return setCommandLong(listener, MAV_CMD.MAV_CMD_DO_SET_MODE, mode.toFloat(), custom.toFloat(), subMode.toFloat())
    }


    fun readStatus(listener: ACKListener<msg_self>): Boolean {
        val msg = msg_command_long()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.command = 452
        msg.param1 = 2.2f
        val handler = CommonACKHandler(sendMessage, msg_self.MAVLINK_MSG_ID_COMMAND_LONG, msg, listener)
        handler.retry = 0
        return sendMessage.send(handler)
    }

    fun readVersion(listener: ACKListener<Versions>?) {
        val msg = msg_command_long()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.command = 459
        msg.param1 = 14.2f
        val handler = ReadVersionACKHandler(sendMessage, msg, listener)
        handler.retry = Integer.MAX_VALUE
        sendMessage.send(handler)
    }

    /**
     * @param task
     * @param listener 响应监听
     */
//    fun sendTask(task: Task, listener: ACKListener<Progress<Pair<IntegerMap, Map<Int, Point>>>>) {
//        sendMessage.send(TaskACKHandler(sendMessage, task, listener, uav.sysid, uav.compid))
//    }

    fun readTask(listener: ACKListener<Progress<HashMap<Int, MAVLinkMessage>>>) {
        sendMessage.send(ReadTaskHandler(sendMessage, uav.sysid, uav.compid, listener))
    }

    fun readParametersList(listener: ACKListener<msg_param_value>) {
        val msg = msg_param_request_list()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        sendMessage.send(CommonACKHandler(sendMessage, msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE, msg, listener))
    }

    fun readParameter(name: String, listener: ACKListener<msg_param_value>): ParamReadACKHandler {
        val msg = msg_param_request_read()
        msg.param_index = -1
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.param_Id = name
        val ackHandler = ParamReadACKHandler(sendMessage, msg, listener)
        sendMessage.send(ackHandler)
        return ackHandler
    }

    /**
     * @param flightSpeed cm/s
     */
    fun setFlightSpeed(flightSpeed: Float, listener: ACKListener<msg_param_value>) {
        setParam(UAV.PARAM_WPNAV_SPEED, flightSpeed, listener)
    }

    /**
     * @param returnAltitude cm
     */
    fun setReturnAltitude(returnAltitude: Float, listener: ACKListener<msg_param_value>) {
        setParam(UAV.PARAM_RTL_ALT, returnAltitude, listener)
    }

    fun calibrationLevel(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION, 0f, 0f, 0f, 0f, 2f)
    }


    fun setCompassCalibrationCancel(listener: ACKListener<msg_command_ack>?) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_DO_CANCEL_MAG_CAL)
    }

    fun setCompassCalibrationAccept(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_DO_ACCEPT_MAG_CAL)
    }

    fun setStartCompass(listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, MAV_CMD.MAV_CMD_DO_START_MAG_CAL, 0f, 0f, 1f)
    }

    fun startCalibrationAccelerometer(listener: ACKListener<msg_command_long>): CalibrationAccelerometer {
        val msg = msg_command_long()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.command = MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION
        msg.param5 = 1f
        val calibrationAccelerometer = CalibrationAccelerometer(sendMessage, msg, listener)
        sendMessage.send(calibrationAccelerometer)
        return calibrationAccelerometer
    }

    fun setCommandAck(cmd: Int, result: Short) {
        val msg = msg_command_ack()
        msg.command = cmd
        msg.result = result
        val handler = CommonACKHandler<msg_heartbeat>(sendMessage, msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT, msg, null)
        handler.retry = 0
        sendMessage.send(handler)
    }

    fun setEventAck(seq: Short) {
        val ack = msg_event_ack()
        ack.sequence = seq
        ack.result = MAV_RESULT.MAV_RESULT_ACCEPTED.toShort()
//        ack.time = System.currentTimeMillis()
        val handler = CommonACKHandler<msg_heartbeat>(sendMessage, msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT, ack, null)
        handler.retry = 0
        sendMessage.send(handler)
    }

    fun setParam(paramId: String, value: Float, listener: ACKListener<msg_param_value>?) {
        val parameter = uav.getParameters(paramId)
        if (parameter == null) {
            readParameter(paramId, ACKListener { result ->
                if (result.isSuccess) {
                    if (result.data.param_value == value) {
                        listener?.onResponse(result)
                    } else {
                        setParam(result.data.param_Id, value, listener)
                    }
                } else {
                    listener?.onResponse(result)
                }
            })
        } else if (parameter.getValue() == value.toDouble()) {
            if (listener != null) {
                val data = msg_param_value()
                data.param_value = value
                data.param_Id = paramId
                data.param_type = parameter.type.toShort()
                listener.onResponse(Response<msg_param_value>().setSuccess(true).setData(data))
            }
        } else {
            val msg = msg_param_set()
            msg.target_system = uav.sysid
            msg.target_component = uav.compid
            msg.param_Id = parameter.name
            msg.param_type = parameter.type.toByte().toShort()
            msg.param_value = value
            sendMessage.send(ParamSetACKHandler(sendMessage, msg, listener))
        }

    }

    /**
     * command long
     */
    fun setCommandLong(listener: ACKListener<msg_command_ack>?, command: Int, vararg param: Float): Boolean {
        val msg = msg_command_long()
        msg.target_system = uav.sysid
        msg.target_component = uav.compid
        msg.command = command
        try {
            msg.param1 = param[0]
            msg.param2 = param[1]
            msg.param3 = param[2]
            msg.param4 = param[3]
            msg.param5 = param[4]
            msg.param6 = param[5]
            msg.param7 = param[6]
        } catch (ignored: Exception) {
        }

        return setCommandLong(msg, listener)
    }

    private fun setCommandLong(msg: msg_command_long, listener: ACKListener<msg_command_ack>?): Boolean {
        return setCommandLong(CommandLongACKHandler(sendMessage, msg, listener))
    }

    private fun setCommandLong(handler: CommandLongACKHandler): Boolean {
        return sendMessage.send(handler)
    }

    fun setRemoteControl(enable: Boolean, listener: ACKListener<msg_command_ack>) {
        setCommandLong(listener, 444, if (enable) 1f else 0f)
    }
}
