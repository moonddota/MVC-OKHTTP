package com.skylin.mavlink.xbee

import com.digi.xbee.api.packet.APIFrameType
import com.digi.xbee.api.packet.GenericXBeePacket
import com.digi.xbee.api.packet.UnknownXBeePacket
import com.digi.xbee.api.packet.XBeePacket
import com.digi.xbee.api.packet.cellular.RXSMSPacket
import com.digi.xbee.api.packet.cellular.TXSMSPacket
import com.digi.xbee.api.packet.common.*
import com.digi.xbee.api.packet.devicecloud.*
import com.digi.xbee.api.packet.ip.RXIPv4Packet
import com.digi.xbee.api.packet.ip.TXIPv4Packet
import com.digi.xbee.api.packet.raw.*
import com.digi.xbee.api.packet.thread.*
import com.digi.xbee.api.packet.wifi.IODataSampleRxIndicatorWifiPacket
import com.digi.xbee.api.packet.wifi.RemoteATCommandResponseWifiPacket
import com.digi.xbee.api.packet.wifi.RemoteATCommandWifiPacket

/**
 * Created by sjj on 2017/12/5.
 */
class Packet(val length: Int) {
    companion object {
        val STX = 0X7E
    }

    private val payload: ByteArray = ByteArray(length)
    private var index = 0

    val id: Int
        get() {
            return payload[0].toInt() and 0xff
        }

    fun add(byte: Byte) {
        payload[index] = byte
        index++
    }

    fun isFilled(): Boolean = index == payload.size
    fun checksum(byte: Byte): Boolean {
        val sum = (0xff - (payload.sum() and 0xff)) and 0xff
        return (byte.toInt() and 0xff) == sum
    }

    fun parsePayload(): XBeePacket? {
        val apiType = APIFrameType.get(payload[0].toInt() and 0xFF) ?: return UnknownXBeePacket.createPacket(payload)

        var packet: XBeePacket? = null
        when (apiType) {
            APIFrameType.TX_64 -> packet = TX64Packet.createPacket(payload)
            APIFrameType.TX_16 -> packet = TX16Packet.createPacket(payload)
            APIFrameType.REMOTE_AT_COMMAND_REQUEST_WIFI -> packet = RemoteATCommandWifiPacket.createPacket(payload)
            APIFrameType.AT_COMMAND -> packet = ATCommandPacket.createPacket(payload)
            APIFrameType.AT_COMMAND_QUEUE -> packet = ATCommandQueuePacket.createPacket(payload)
            APIFrameType.TRANSMIT_REQUEST -> packet = TransmitPacket.createPacket(payload)
            APIFrameType.EXPLICIT_ADDRESSING_COMMAND_FRAME -> packet = ExplicitAddressingPacket.createPacket(payload)
            APIFrameType.REMOTE_AT_COMMAND_REQUEST -> packet = RemoteATCommandPacket.createPacket(payload)
            APIFrameType.IPV6_REMOTE_AT_COMMAND_REQUEST -> packet = IPv6RemoteATCommandRequestPacket.createPacket(payload)
            APIFrameType.TX_SMS -> packet = TXSMSPacket.createPacket(payload)
            APIFrameType.TX_IPV4 -> packet = TXIPv4Packet.createPacket(payload)
            APIFrameType.TX_IPV6 -> packet = TXIPv6Packet.createPacket(payload)
            APIFrameType.SEND_DATA_REQUEST -> packet = SendDataRequestPacket.createPacket(payload)
            APIFrameType.DEVICE_RESPONSE -> packet = DeviceResponsePacket.createPacket(payload)
            APIFrameType.RX_64 -> packet = RX64Packet.createPacket(payload)
            APIFrameType.RX_16 -> packet = RX16Packet.createPacket(payload)
            APIFrameType.RX_IPV6 -> packet = RXIPv6Packet.createPacket(payload)
            APIFrameType.RX_IO_64 -> packet = RX64IOPacket.createPacket(payload)
            APIFrameType.RX_IO_16 -> packet = RX16IOPacket.createPacket(payload)
            APIFrameType.REMOTE_AT_COMMAND_RESPONSE_WIFI -> packet = RemoteATCommandResponseWifiPacket.createPacket(payload)
            APIFrameType.AT_COMMAND_RESPONSE -> packet = ATCommandResponsePacket.createPacket(payload)
            APIFrameType.TX_STATUS -> packet = TXStatusPacket.createPacket(payload)
            APIFrameType.MODEM_STATUS -> packet = ModemStatusPacket.createPacket(payload)
            APIFrameType.TRANSMIT_STATUS -> packet = TransmitStatusPacket.createPacket(payload)
            APIFrameType.IO_DATA_SAMPLE_RX_INDICATOR_WIFI -> packet = IODataSampleRxIndicatorWifiPacket.createPacket(payload)
            APIFrameType.RECEIVE_PACKET -> packet = com.digi.xbee.api.packet.common.ReceivePacket.createPacket(payload)
            APIFrameType.EXPLICIT_RX_INDICATOR -> packet = ExplicitRxIndicatorPacket.createPacket(payload)
            APIFrameType.IO_DATA_SAMPLE_RX_INDICATOR -> packet = IODataSampleRxIndicatorPacket.createPacket(payload)
            APIFrameType.IPV6_IO_DATA_SAMPLE_RX_INDICATOR -> packet = IPv6IODataSampleRxIndicator.createPacket(payload)
            APIFrameType.REMOTE_AT_COMMAND_RESPONSE -> packet = RemoteATCommandResponsePacket.createPacket(payload)
            APIFrameType.IPV6_REMOTE_AT_COMMAND_RESPONSE -> packet = IPv6RemoteATCommandResponsePacket.createPacket(payload)
            APIFrameType.RX_SMS -> packet = RXSMSPacket.createPacket(payload)
            APIFrameType.RX_IPV4 -> packet = RXIPv4Packet.createPacket(payload)
            APIFrameType.SEND_DATA_RESPONSE -> packet = SendDataResponsePacket.createPacket(payload)
            APIFrameType.DEVICE_REQUEST -> packet = DeviceRequestPacket.createPacket(payload)
            APIFrameType.DEVICE_RESPONSE_STATUS -> packet = DeviceResponseStatusPacket.createPacket(payload)
            APIFrameType.COAP_TX_REQUEST -> packet = CoAPTxRequestPacket.createPacket(payload)
            APIFrameType.COAP_RX_RESPONSE -> packet = CoAPRxResponsePacket.createPacket(payload)
            APIFrameType.FRAME_ERROR -> packet = FrameErrorPacket.createPacket(payload)
            APIFrameType.GENERIC -> packet = GenericXBeePacket.createPacket(payload)
            APIFrameType.UNKNOWN -> packet = UnknownXBeePacket.createPacket(payload)
            else -> packet = UnknownXBeePacket.createPacket(payload)
        }
        return packet
    }
}