package com.skylin.mavlink.handler.ack

import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.common.msg_command_ack
import com.MAVLink.common.msg_command_long
import com.MAVLink.enums.ACCELCAL_VEHICLE_POS
import com.MAVLink.enums.MAV_CMD
import com.MAVLink.enums.MAV_RESULT
import com.skylin.mavlink.ACKListener
import com.skylin.mavlink.SendMessage
import com.skylin.mavlink.handler.ACK.AbstractACKHandler
import com.skylin.mavlink.model.Response
import com.skylin.mavlink.utils.resultText

class CalibrationAccelerometer(sendMavPack: SendMessage, private val msg: msg_command_long, listener: ACKListener<msg_command_long>) : AbstractACKHandler<msg_command_long>(sendMavPack, msg, listener) {
    override fun requestAck(): Boolean {
        return sendMessage(this)
    }

    override fun onSend() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancel() {
        active = false
    }

    override fun handleMsg(message: MAVLinkMessage?): Boolean {
        if (message is msg_command_ack && message.command == msg.command) {
            if (message.result == MAV_RESULT.MAV_RESULT_ACCEPTED.toShort()) {
//                response(Response<String>().setSuccess(true))
            } else {
                response(Response<msg_command_long>().setSuccess(false).setErrorMessage(message.resultText))
                return true
            }
        }

        if (message is msg_command_long && message.command == MAV_CMD.MAV_CMD_ACCELCAL_VEHICLE_POS) {
            if (message.param1 == ACCELCAL_VEHICLE_POS.ACCELCAL_VEHICLE_POS_FAILED.toFloat()) {
                response(Response<msg_command_long>().setSuccess(false).setErrorMessage("Calibration failed"))
                return true
            }
            response(Response<msg_command_long>().setSuccess(true).setData(message))
            if (message.param1 == ACCELCAL_VEHICLE_POS.ACCELCAL_VEHICLE_POS_SUCCESS.toFloat()) {
                return true
            }
            retry = 0
            setTimeout(60000)
        }

//        if (message is msg_statustext && message.severity == MAV_SEVERITY.MAV_SEVERITY_CRITICAL) {
//            val text = message.getText()
//            if (text.contains("calibration failed", true)) {
//                response(Response<msg_command_long>().setSuccess(false).setErrorMessage(text))
//                return true
//            }
//        }
        return false
    }
}