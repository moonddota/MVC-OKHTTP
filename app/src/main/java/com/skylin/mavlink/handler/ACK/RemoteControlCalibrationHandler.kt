package com.skylin.mavlink.handler.ack

import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.common.msg_rc_channels
import com.MAVLink.common.msg_rc_channels_raw
import com.MAVLink.common.msg_request_data_stream
import com.skylin.mavlink.ACKListener
import com.skylin.mavlink.SendMessage
import com.skylin.mavlink.handler.ACK.AbstractACKHandler
import com.skylin.mavlink.model.Response


class RemoteControlCalibrationHandler(sendMavPack: SendMessage, private val message: msg_request_data_stream, listener: ACKListener<msg_rc_channels>) : AbstractACKHandler<msg_rc_channels>(sendMavPack, message, listener) {
    private var cancel = false
    private var cancelTime = 0L

    init {
        retry = Integer.MAX_VALUE
    }

    override fun requestAck(): Boolean {
        return sendMessage(this)
    }

    override fun onSend() {

    }

    override fun cancel() {
        message.req_message_rate = 0
        message.start_stop = 0
        sendMessage(this)
        setTimeout(5000)
        cancelTime = System.currentTimeMillis()
        cancel = true
    }

    override fun timeout(): Boolean {
        return !cancel && super.timeout()
    }

    override fun handleMsg(message: MAVLinkMessage): Boolean {
        if (cancel) {
            if (System.currentTimeMillis() - 1000 > cancelTime && message is msg_rc_channels_raw || message is msg_rc_channels) {
                cancelTime = System.currentTimeMillis()
                sendMessage(this)
                return true
            }
            if (super.timeout()) {
                return true
            }
        } else {
            if (message is msg_rc_channels) {
                setTimeout(5000)
                response(Response<msg_rc_channels>().setData(message).setSuccess(true))
            }
        }
        return false
    }
}