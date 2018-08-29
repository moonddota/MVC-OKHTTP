package com.skylin.mavlink.model

import com.MAVLink.ardupilotmega.msg_user_status
import java.io.Serializable

data class UavStatus(var time: Long, var voltage: Float, var lequidlevel: Short) :Serializable{
    companion object {
        @JvmStatic
        fun create(msg: msg_user_status): UavStatus {
            return UavStatus(msg.time, msg.voltage, msg.lequidlevel)
        }
    }
}