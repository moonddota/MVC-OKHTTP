package com.skylin.mavlink.utils

import com.MAVLink.common.msg_command_ack
import com.MAVLink.enums.MAV_RESULT


val msg_command_ack.resultText
    get() = when (result.toInt()) {
        MAV_RESULT.MAV_RESULT_ACCEPTED -> "设置成功"
        MAV_RESULT.MAV_RESULT_TEMPORARILY_REJECTED -> "设置被临时拒绝"
        MAV_RESULT.MAV_RESULT_DENIED -> "设置被拒绝"
        MAV_RESULT.MAV_RESULT_UNSUPPORTED -> "不支持的设置"
        MAV_RESULT.MAV_RESULT_FAILED -> "设置失败"
        MAV_RESULT.MAV_RESULT_ENUM_END -> "ENUM_END"
        else -> "unknown result：$result"
    }
val msg_command_ack.isAccepted
    get() = result.toInt() == MAV_RESULT.MAV_RESULT_ACCEPTED
