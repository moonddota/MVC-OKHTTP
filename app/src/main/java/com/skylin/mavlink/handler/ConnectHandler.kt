package com.skylin.mavlink.handler

import com.MAVLink.common.msg_command_ack
import com.digi.xbee.api.models.*
import com.digi.xbee.api.packet.XBeeAPIPacket
import com.digi.xbee.api.packet.XBeePacket
import com.digi.xbee.api.packet.common.ATCommandPacket
import com.digi.xbee.api.packet.common.ATCommandResponsePacket
import com.digi.xbee.api.packet.common.TransmitPacket
import com.digi.xbee.api.packet.common.TransmitStatusPacket
import com.skylin.mavlink.message.msg_mac

/**
 * Created by sjj on 2018/3/6.
 */
class ConnectHandler(val reset: Boolean = false, val target: String) {
    private val REQUEST_MAC_SH = 0
    private val REQUEST_MAC_SL = 1
    private val REQUEST_CONNECT = 2
    private var status = if (reset) REQUEST_CONNECT else REQUEST_MAC_SH
    private var lastSeq = -1
    private var mac_sh: ByteArray = byteArrayOf(0, 0, 0, 0) //= HexUtils.hexStringToByteArray("0013A200")
    private var mac_sl: ByteArray = byteArrayOf(0, 0, 0, 0) //= HexUtils.hexStringToByteArray("41274777")
    private var timeOut: Long = 0

    fun generateByteArray(seq: Int): ByteArray {
        lastSeq = seq
        return when (status) {
            REQUEST_MAC_SH -> {
                val nullBy: ByteArray? = null
                val packet = ATCommandPacket(seq, "SH", nullBy)
                setTimeOut(1800)
                packet.generateByteArray()
            }
            REQUEST_MAC_SL -> {
                val nullBy: ByteArray? = null
                val packet = ATCommandPacket(seq, "SL", nullBy)
                setTimeOut(1800)
                packet.generateByteArray()
            }
            REQUEST_CONNECT -> {
                val msg = msg_mac(mac_sh, mac_sl)
                setTimeOut(10000)
                TransmitPacket(seq, XBee64BitAddress(target), XBee16BitAddress.UNKNOWN_ADDRESS, 0, XBeeTransmitOptions.NONE, msg.pack().encodePacket()).generateByteArray()
            }
            else -> throw IllegalStateException()
        }
    }

    fun handle(m: TransmitStatusPacket): Boolean {
        if (status == REQUEST_CONNECT && m.checkFrameID(lastSeq)) {
            if (m.transmitStatus == XBeeTransmitStatus.SUCCESS) {
                return true
            } else {
                setTimeOut(0)
                return false
            }
        }

        return false
    }

    fun handle(o: ATCommandResponsePacket): Boolean {
        if (o.checkFrameID(lastSeq)) {
            when (status) {
                REQUEST_MAC_SH -> {
                    mac_sh = o.commandValue
                    status = REQUEST_MAC_SL
                    setTimeOut(0)
                    return true
                }
                REQUEST_MAC_SL -> {
                    mac_sl = o.commandValue
                    status = REQUEST_CONNECT
                    setTimeOut(0)
                    return true
                }
            }
        }
        return false
    }

    private fun setTimeOut(time: Long) {
        timeOut = System.currentTimeMillis() + time
    }

    fun isTimeOut(): Boolean {
        return timeOut <= System.currentTimeMillis()
    }

}