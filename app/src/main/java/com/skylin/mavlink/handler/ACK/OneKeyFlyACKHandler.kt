package com.skylin.mavlink.handler.ack

import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.common.msg_command_ack
import com.MAVLink.common.msg_command_long
import com.MAVLink.common.msg_self
import com.MAVLink.common.msg_statustext
import com.MAVLink.enums.MAV_RESULT
import com.skylin.mavlink.ACKListener
import com.skylin.mavlink.SendMessage
import com.skylin.mavlink.handler.ACK.CommandLongACKHandler
import com.skylin.mavlink.model.Response
import com.skylin.mavlink.utils.isTakeoffNotice
import com.skylin.mavlink.utils.resultText
import com.skylin.mavlink.utils.warning

/**
 * Created by sjj on 2018/2/24.
 */
class OneKeyFlyACKHandler(sendMavPack: SendMessage?, private val message: msg_command_long, responseListener: ACKListener<msg_command_ack>?) : CommandLongACKHandler(sendMavPack, message, responseListener) {
    private var count = 0
    init {
        retry = 0
    }

    override fun handleMsg(message: MAVLinkMessage?): Boolean {
        if (message is msg_self ) {
            if (message.onekeyfly == msg_self.ONE_KEY_SUCCESS) {
                val msg = msg_command_ack()
                msg.command = this.message.command
                msg.result = MAV_RESULT.MAV_RESULT_ACCEPTED.toShort()
                response(Response<msg_command_ack>().setData(msg).setSuccess(true))
                return true
            }
            count++
            if (count == 10) {
                response(Response<msg_command_ack>().setErrorMessage(message.oneKeyFlyText).setSuccess(false))
                return true
            }
        }
        if (message is msg_statustext && message.isTakeoffNotice) {
            response(Response<msg_command_ack>().setSuccess(false).setErrorMessage(message.warning))
            return true
        }
        if (message !is msg_command_ack) {
            return false
        }
        if (this.message.command == message.command && message.result != MAV_RESULT.MAV_RESULT_ACCEPTED.toShort()) {
            response(Response<msg_command_ack>().setErrorMessage(message.resultText).setSuccess(false))
            return true
        }
        return false
    }
}