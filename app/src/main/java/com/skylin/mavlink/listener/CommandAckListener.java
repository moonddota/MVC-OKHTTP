package com.skylin.mavlink.listener;

import com.MAVLink.common.msg_command_ack;
import com.MAVLink.enums.MAV_RESULT;

/**
 * Created by SJJ on 2017/6/30.
 */

public abstract class CommandAckListener extends AbstractACKListener<msg_command_ack> {
    @Override
    public void onSuccess(msg_command_ack commandAck) {
        if (commandAck.result == MAV_RESULT.MAV_RESULT_ACCEPTED) {
            onAccepted();
        } else {
            onReject(commandAck);
        }
    }

    public abstract void onAccepted();

    public abstract void onReject(msg_command_ack ack);
}
