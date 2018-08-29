package com.skylin.mavlink.listener;

import com.MAVLink.common.msg_command_ack;
import com.skylin.uav.drawforterrain.util.ToastUtil;

/**
 * Created by sjj on 2017/7/6.
 */

public abstract class CommandToastAckListener extends CommandAckListener {
    private final String prefix;

    public CommandToastAckListener(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onFailed(String errorMessage) {
        ToastUtil.show(prefix + "：" + errorMessage);
    }

    @Override
    public void onReject(msg_command_ack ack) {
        ToastUtil.show(prefix + "：" );
    }
}
