package com.skylin.mavlink.model

import com.MAVLink.common.msg_gps_raw_int
import com.MAVLink.enums.GPS_FIX_TYPE
import java.io.Serializable

data class GpsState(var fixType: Int = GPS_FIX_TYPE.GPS_FIX_TYPE_NO_GPS, var gpsSatCount: Int = 0,
                    /**
                     * gps_eph(m)
                     */
                    var eph: Float = 0f):Serializable {
    val fixTypeText: String = when (fixType) {
        GPS_FIX_TYPE.GPS_FIX_TYPE_NO_GPS -> "GPS未连接"
        GPS_FIX_TYPE.GPS_FIX_TYPE_NO_FIX -> "GPS未定位"
        GPS_FIX_TYPE.GPS_FIX_TYPE_2D_FIX -> "2D定位"
        GPS_FIX_TYPE.GPS_FIX_TYPE_3D_FIX -> "3D定位"
        GPS_FIX_TYPE.GPS_FIX_TYPE_DGPS -> "辅助定位"
        GPS_FIX_TYPE.GPS_FIX_TYPE_RTK_FLOAT -> "浮动"
        GPS_FIX_TYPE.GPS_FIX_TYPE_RTK_FIXED -> "固定"
        GPS_FIX_TYPE.GPS_FIX_TYPE_STATIC -> "静态定位"
        else -> "未知类型:$fixType"
    }

    companion object {
        @JvmStatic
        fun create(msg: msg_gps_raw_int): GpsState {
            return GpsState(msg.fix_type.toInt(), msg.satellites_visible.toInt(), msg.eph.toFloat() / 100)
        }
    }
}