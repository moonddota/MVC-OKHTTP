package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_request_data_stream;
import com.skylin.mavlink.SendMessage;

/**
 * Created by SJJ on 2017/4/29.
 */

public class StreamACKHandler extends CommonACKHandler {
    private msg_request_data_stream message;
    private boolean once;

    public StreamACKHandler(SendMessage sendMavPack, int targetMsgId, msg_request_data_stream message, boolean once, com.skylin.mavlink.ACKListener responseListener) {
        super(sendMavPack, targetMsgId, message, responseListener);
        this.message = message;
        this.once = once;
    }

    @Override
    public boolean handleMsg(MAVLinkMessage message) {
        boolean b = super.handleMsg(message);
        if (b && once) {
            this.message.start_stop = 0;
            sendMessage(this);
        }
        return b;
    }
}
