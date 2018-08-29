package com.skylin.mavlink.model

import com.MAVLink.common.msg_global_position_int

data class GlobalPosition(var lat: Double, var lon: Double, var alt: Double, var yaw: Double) {
    val point: Point get() = Point(lon, lat, alt)

    companion object {
        @JvmStatic
        fun create(global: msg_global_position_int) = GlobalPosition(global.lat / 1e7, global.lon / 1e7, global.alt / 1e3, global.hdg / 100.0)
    }
}
