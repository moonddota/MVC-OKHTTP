package com.skylin.mavlink.utils

import com.MAVLink.common.msg_mission_ack
import com.MAVLink.enums.MAV_MISSION_RESULT

val msg_mission_ack.typeText: String
    get() = typeText(type)

fun typeText(result: Short): String {
    return when (result.toInt()) {
        MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED -> "设置成功"
        MAV_MISSION_RESULT.MAV_MISSION_ERROR -> "任务指令被拒绝"
        MAV_MISSION_RESULT.MAV_MISSION_UNSUPPORTED_FRAME -> "不支持该坐标系"
        MAV_MISSION_RESULT.MAV_MISSION_UNSUPPORTED -> "不支持该命令"
        MAV_MISSION_RESULT.MAV_MISSION_NO_SPACE -> "任务数过长"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID -> "参数设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_PARAM1 -> "PARAM1设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_PARAM2 -> "PARAM2设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_PARAM3 -> "PARAM3设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_PARAM4 -> "PARAM4设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_PARAM5_X -> "PARAM5设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_PARAM6_Y -> "PARAM6设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_PARAM7 -> "PARAM7设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_INVALID_SEQUENCE -> "任务序列号设置错误"
        MAV_MISSION_RESULT.MAV_MISSION_DENIED -> "飞控不接受任何指令"
        else -> "未知返回码"
    }
}