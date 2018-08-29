package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Response;

/**
 * Created by SJJ on 2017/4/12.
 */

public class CommonACKHandler<T extends MAVLinkMessage> extends AbstractACKHandler<T> {
    protected final int targetMsgId;

    public CommonACKHandler(SendMessage sendMavPack, int targetMsgId, MAVLinkMessage message, com.skylin.mavlink.ACKListener<T> responseListener) {
        super(sendMavPack, message, responseListener);
        this.targetMsgId = targetMsgId;
    }
    @Override
    public boolean requestAck() {
        return sendMessage(this);
    }

    @Override
    public void onSend() {
    }
    @Override
    public boolean handleMsg(MAVLinkMessage message) {
        if (message.msgid != targetMsgId) {
            return false;
        }
        response(new Response<T>().setData((T) message).setSuccess(true));
        return true;
    }
}
