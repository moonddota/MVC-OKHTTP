package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_self;
import com.skylin.mavlink.SendMessage;

/**
 * Created by sjj on 2017/4/21.
 */
@Deprecated
public class UnLockACKHandler extends CommonACKHandler<msg_self> {
    public UnLockACKHandler(SendMessage sendMavPack, int targetMsgId, MAVLinkMessage message, com.skylin.mavlink.ACKListener<msg_self> responseListener) {
        super(sendMavPack, targetMsgId, message, responseListener);
    }
}
