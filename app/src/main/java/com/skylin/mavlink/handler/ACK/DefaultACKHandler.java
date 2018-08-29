package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.skylin.mavlink.SendMessage;

/**
 * Created by SJJ on 2017/4/12.
 */

public class DefaultACKHandler<T> extends AbstractACKHandler<T> {
    public DefaultACKHandler(SendMessage sendMavPack, MAVLinkMessage message) {
        super(sendMavPack, message, null);
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
        return true;
    }
}
