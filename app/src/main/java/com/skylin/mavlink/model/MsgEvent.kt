package com.skylin.mavlink.model

import com.MAVLink.ardupilotmega.msg_vip_events
import com.MAVLink.enums.VIP_EVENTS_NAME
import com.MAVLink.enums.VIP_EVENTS_TYPE

data class MsgEvent(val time: Long, val type: Short, val name: Short, val value: Short) {
    fun isError() = type.toInt() == VIP_EVENTS_TYPE.MAV_VIP_EVENT_ERROR
    fun des() = when (name.toInt()) {
        VIP_EVENTS_NAME.MAV_VIP_EVENT_LOW_POWER -> "电量低"
        VIP_EVENTS_NAME.MAV_VIP_EVENT_RC_LOST -> "遥控器连接已断开"
        VIP_EVENTS_NAME.MAV_VIP_EVENT_LOW_YW -> "药量低"
        VIP_EVENTS_NAME.MAV_VIP_EVENT_TAKEOFF_ARMING_ERROR -> "起飞：解锁失败"
        VIP_EVENTS_NAME.MAV_VIP_EVENT_TAKEOFF_SET_STABLIZE_ERROR -> "起飞：STABLIZE模式设置失败"
        VIP_EVENTS_NAME.MAV_VIP_EVENT_TAKEOFF_SET_AUTO_ERROR -> "起飞：AUTO模式设置失败"
        VIP_EVENTS_NAME.MAV_VIP_EVENT_TAKEOFF_SUCCESS -> "起飞成功"
        else -> "未知：$name"
    }

    override fun toString(): String {
        return "MsgEvent(time=$time, type=$type, name=$name, value=$value, ${des()})"
    }


    companion object {
        @JvmStatic
        fun create(msg: msg_vip_events): MsgEvent {
           return MsgEvent(msg.time, msg.type, msg.name, msg.value)
        }
    }
}