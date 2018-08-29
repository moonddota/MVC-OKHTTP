package com.skylin.mavlink.xbee

import com.digi.xbee.api.packet.XBeePacket

/**
 * Created by sjj on 2017/12/5.
 */
class XBeeParser {
    private var status = STATE.IDLE
    private var length: Int = 0
    private var packet: Packet? = null
    fun parse(byte: Byte): XBeePacket? {
        when (status) {
            XBeeParser.STATE.IDLE -> {
                if ((byte.toInt() and 0x00ff) == Packet.STX) {
                    status = STATE.GOT_STX
                }
            }
            XBeeParser.STATE.GOT_STX -> {
                length = byte.toInt() and 0xff shl 8
                status = STATE.GOT_HL
            }
            XBeeParser.STATE.GOT_HL -> {
                length = byte.toInt() and 0xff or length
                status = STATE.GOT_LL
                packet = Packet(length)
            }
            XBeeParser.STATE.GOT_LL -> {
                if (packet != null) {
                    packet!!.add(byte)
                    if (packet!!.isFilled()) {
                        status = STATE.GOT_PAYLOAD
                    }
                } else {
                    status = STATE.IDLE
                }
            }
            XBeeParser.STATE.GOT_PAYLOAD -> {
                status = STATE.IDLE
                if (packet?.checksum(byte) == true) {
                    return packet?.parsePayload()
                }
            }
        }
        return null
    }

    fun reset() {
        status = STATE.IDLE
    }

    private enum class STATE {
        IDLE, GOT_STX, GOT_HL, GOT_LL, GOT_PAYLOAD
    }
}