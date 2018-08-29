package com.skylin.mavlink.message

import com.MAVLink.MAVLinkPacket
import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.Messages.MAVLinkPayload
import com.MAVLink.common.msg_command_long
import com.digi.xbee.api.utils.HexUtils
import sjj.alog.Log

/**
 * Created by sjj on 2017/12/13.
 */
class msg_mac(private val mac_sh: ByteArray, private val mac_sl: ByteArray) : msg_command_long() {
    init {
        if (mac_sh.size != 4 || mac_sl.size != 4) {
            throw IllegalArgumentException("mac 地址必须是 4 位byte")
        }
        command = 463
        param1 = toInt(true, mac_sh).toFloat()
        param2 = toInt(false, mac_sh).toFloat()
        param3 = toInt(true, mac_sl).toFloat()
        param4 = toInt(false, mac_sl).toFloat()
    }

    private fun toInt(h: Boolean, array: ByteArray): Int {
        val start = if (h) 0 else 2
        var value = 0
        value = value or (array[start].toInt() and 0xff shl 8)
        value = value or (array[start + 1].toInt() and 0xff)
        return value
    }
}