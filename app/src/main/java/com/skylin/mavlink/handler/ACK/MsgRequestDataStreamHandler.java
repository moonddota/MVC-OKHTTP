package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_request_data_stream;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Response;

public class MsgRequestDataStreamHandler extends AbstractACKHandler<MAVLinkMessage> {

    private int targetMsgId;
    private msg_request_data_stream message;

    public MsgRequestDataStreamHandler(SendMessage sendMavPack, int targetMsgId, msg_request_data_stream message) {
        super(sendMavPack, message, null);
        this.targetMsgId = targetMsgId;
        this.message = message;

        if (message.start_stop == 0) {
            setTimeout(20000);
        } else {
            setRetry(Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean requestAck() {
        return sendMessage(this);
    }

    @Override
    public void onSend() {

    }

    @Override
    public synchronized boolean timeout() {
        return false;
    }

    @Override
    public boolean handleMsg(MAVLinkMessage message) {
        if (this.message.start_stop == 0) {
            if (message.msgid == targetMsgId) {
                sendMessage(this);
                setTimeout(3000);
                resetRetry();
            }
            if (super.timeout()) {
                response(new Response<MAVLinkMessage>().setSuccess(true));
                return true;
            }
        } else  {
            if (message.msgid == targetMsgId) {
                response(new Response<MAVLinkMessage>().setData(message).setSuccess(true));
                return true;
            }
        }
        return false;
    }
}