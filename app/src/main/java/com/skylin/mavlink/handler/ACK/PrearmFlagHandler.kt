package com.skylin.mavlink.handler.ack

import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.ardupilotmega.msg_prearm_flag
import com.MAVLink.common.msg_request_data_stream
import com.skylin.mavlink.ACKListener
import com.skylin.mavlink.SendMessage
import com.skylin.mavlink.handler.ACK.AbstractACKHandler

class PrearmFlagHandler(send: SendMessage,private val msg: msg_request_data_stream, listener: ACKListener<msg_prearm_flag>?) : AbstractACKHandler<msg_prearm_flag>(send, msg, listener) {
    init {
        retry = Int.MAX_VALUE
    }

    override fun handleMsg(message: MAVLinkMessage): Boolean {
        if (message is msg_prearm_flag) {
            setTimeout(10000)
            if (message.value == 0L) {
                msg.req_message_rate = 0
                msg.start_stop = 0
                sendMessage(this)
                return true
            }
        }

        return false
    }
}