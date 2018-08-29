package com.skylin.mavlink.listener;

import com.MAVLink.common.msg_command_ack;
import com.skylin.mavlink.utils.Msg_command_acksKt;

/**
 * Created by sjj on 2017/7/6.
 */

public class CommandNotificationAckListener extends CommandAckListener {
    private final String prefix;

    public CommandNotificationAckListener(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onAccepted() {
        notification(prefix,"设置成功");
    }

    @Override
    public void onFailed(String errorMessage) {
        notification(prefix,errorMessage);
    }

    @Override
    public void onReject(msg_command_ack ack) {
        notification(prefix, Msg_command_acksKt.getResultText(ack));
    }
}
