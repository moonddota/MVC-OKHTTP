package com.skylin.mavlink.model

import com.MAVLink.common.msg_mission_current

data class CurrentMission(var lon: Double = 104.0, var lat: Double = 34.0, var alt: Double = 0.0, var seq: Int = 0, var isValid: Boolean = false) {
    val targetPoint: Point
        get() = Point(lon, lat, alt)

    companion object {
        @JvmStatic
        fun create(msg: msg_mission_current): CurrentMission {
            return CurrentMission(msg.lon / 1e7, msg.lat / 1e7, msg.alt / 1e3, msg.seq, msg.effective.toInt() != 0)
        }
    }
}