package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.enums.MAV_RESULT;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Response;
import com.skylin.mavlink.utils.Msg_command_acksKt;

import sjj.alog.Log;

/**
 * Created by Administrator on 2017/5/5.
 */

public class ReadYAWACKHandler extends AbstractACKHandler<Float> {
    private msg_command_long message;

    public ReadYAWACKHandler(SendMessage sendMavPack, msg_command_long message, com.skylin.mavlink.ACKListener<Float> responseListener) {
        super(sendMavPack, message, responseListener);
        this.message = message;
    }

    @Override
    public boolean requestAck() {
        sendMessage(this);
        return false;
    }

    @Override
    public void onSend() {

    }

    @Override
    public boolean handleMsg(MAVLinkMessage message) {
        if (message.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) {
            msg_statustext msg_statustext = (com.MAVLink.common.msg_statustext) message;
            String text = msg_statustext.getText();

            try {
                float parseFloat = Float.parseFloat(text);
                response(new Response<Float>().setData(parseFloat).setSuccess(true));
                return true;
            } catch (Exception e) {
                Log.e("", e);
            }
        }
        if (message.msgid == msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK) {
            msg_command_ack ack = (msg_command_ack) message;
            if (ack.command == this.message.command) {
                if (ack.result != MAV_RESULT.MAV_RESULT_ACCEPTED) {
                    response(new Response<Float>().setSuccess(false).setErrorMessage(Msg_command_acksKt.getResultText(ack)));
                    return true;
                }
            }
        }

        return false;
    }
}
