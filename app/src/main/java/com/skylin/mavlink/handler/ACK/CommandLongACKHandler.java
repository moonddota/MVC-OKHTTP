package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Response;

/**
 * Created by sjj on 2017/4/21.
 * 一键起飞
 */

public class CommandLongACKHandler extends AbstractACKHandler<msg_command_ack> {
    private msg_command_long message;
    public CommandLongACKHandler(SendMessage sendMavPack, msg_command_long message, com.skylin.mavlink.ACKListener<msg_command_ack> responseListener) {
        super(sendMavPack, message, responseListener);
        this.message = message;
    }

    @Override
    public boolean requestAck() {
        sendMessage(this);
        return true;
    }

    @Override
    public void onSend() {

    }

    @Override
    public boolean handleMsg(MAVLinkMessage message) {
        if (message.msgid != msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK) {
            return false;
        }
        msg_command_ack msg = (msg_command_ack) message;
        if (this.message.command == msg.command) {
            response(new Response<msg_command_ack>().setData(msg).setSuccess(true));
        }
        return this.message.command == msg.command;
    }
}
